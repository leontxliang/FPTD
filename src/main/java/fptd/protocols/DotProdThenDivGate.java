package fptd.protocols;

import fptd.Params;
import fptd.Share;
import fptd.offline.OfflineDivisionGate;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DotProdThenDivGate extends Gate {
    //Fields for multiply
    //每一行表示一个dot product的随机数
    private List<List<Share>> a_shr_matrix = new ArrayList<>();
    private List<List<Share>> b_shr_matrix = new ArrayList<>();
    private List<List<Share>> c_shr_matrix = new ArrayList<>();
    private List<List<BigInteger>> delta_x_clear_matrix = new ArrayList<>();
    private List<List<BigInteger>> delta_y_clear_matrix = new ArrayList<>();

    private List<Gate> xGates = new ArrayList<>(); // The i-th xGate dot-product the i-th yGate
    private List<Gate> yGates = new ArrayList<>();

    //Fields for division
    private OutputGate divisorGate; //Get the divisors from this gate
    private List<Share> r_list;
    private List<Share> r1_list;
    private List<Share> r2_list;
    private List<BigInteger> divisors;

    private int num_dot_prod = -1;//计算dot product的个数

    private BigInteger scaling = null; // To scale the result of all dot product

    private List<List<Boolean>> filterMatrix = null; // 0 or 1 values, if 0 then ignore this value in dot product

    /**
     * 有dim次内积，每一次内积的结果是一个数字，因此，需要使用combine Gate来合并这个内积的output gates，以升维
     * @param inputGates include X gates, then Y gates, finally on divisorGate,
     *                   e.g., gateX1, gateX2, ..., gateX_dim, gateY1, gateY2, ..., gateY_dim, divisorGate
     *        divisorGate 只有1维
     */
    public DotProdThenDivGate(List<Gate> inputGates) {
        super(inputGates.toArray(new Gate[inputGates.size()]));
        if(inputGates.size() < 3) {
            throw new IllegalArgumentException("wrong number of inputs");
        }
        this.num_dot_prod = (inputGates.size()-1) / 2;
        this.divisorGate = (OutputGate) inputGates.getLast();
        for(int i = 0; i < num_dot_prod; i++){
            this.xGates.add(inputGates.get(i));
        }
        for(int i = num_dot_prod; i < inputGates.size()-1; i++){
            this.yGates.add(inputGates.get(i));
        }
        this.dim = num_dot_prod;
    }

    private <T> List<Boolean> initFilter(List<T> xList, List<T> yList){
        List<Boolean> filter = new ArrayList<>();
        for(int i = 0; i < xList.size(); i++) {
            if(xList.get(i) != null && yList.get(i) != null) {
                filter.add(true);
            }else{
                filter.add(false);
            }
        }
        return filter;
    }

    public DotProdThenDivGate(List<Gate> inputGates, BigInteger scaling) {
        this(inputGates);
        this.scaling = scaling;
    }

    @Override
    void doReadOfflineFromFile() {
        //randomness for multiply
        for(int dpIdx = 0; dpIdx < num_dot_prod; dpIdx++) {
            a_shr_matrix.add(edgeServer.readRandShares(this.firstGate().dim));
            b_shr_matrix.add(edgeServer.readRandShares(this.firstGate().dim));
            c_shr_matrix.add(edgeServer.readRandShares(this.firstGate().dim));
            delta_x_clear_matrix.add(edgeServer.readClear(this.firstGate().dim));
            delta_y_clear_matrix.add(edgeServer.readClear(this.firstGate().dim));
        }
        //randomness for division
        r_list = edgeServer.readRandShares(num_dot_prod);
        r1_list = edgeServer.readRandShares(num_dot_prod);
        r2_list = edgeServer.readRandShares(num_dot_prod);
    }

    @Override
    void doRunOnline() {
        filterMatrix = new ArrayList<>();//initialize the filterMatrix
        for(int i = 0; i < this.xGates.size(); i++){
            List<Boolean> filter = initFilter(this.xGates.get(i).Delta_clear_list, this.yGates.get(i).Delta_clear_list);
            filterMatrix.add(filter);
        }

        if(this.divisorGate != null){
            List<BigInteger> outputValues = this.divisorGate.getOutputValues();
            //Copy第一个元素填充其他位置，使其长度为dim
            for(int i = outputValues.size(); i < num_dot_prod; i++){
                outputValues.add(outputValues.getFirst());
//                System.out.println("divisor in DivGate = " + outputValues.getFirst());
            }
            this.divisors = outputValues;
        }

        List<Share> Delta_z_share_list = new ArrayList<>();
        List<BigInteger> temp_xy_list = new ArrayList<>();
        final int numDotProd = num_dot_prod;
        for(int pdIdx = 0; pdIdx < numDotProd; pdIdx++) {//For each dot product
            Gate gateX = this.xGates.get(pdIdx);
            Gate gateY = this.yGates.get(pdIdx);
            // temp_x = $\Delta_x + \delta_x$
            List<BigInteger> temp_x = LinearAlgebra.addBigIntVec(gateX.Delta_clear_list,
                    this.delta_x_clear_matrix.get(pdIdx));
            // temp_y = $\Delta_y + \delta_y$
            List<BigInteger> temp_y = LinearAlgebra.addBigIntVec(gateY.Delta_clear_list,
                    this.delta_y_clear_matrix.get(pdIdx));
            //perform the filtering
            temp_x = LinearAlgebra.doFilter(temp_x, this.filterMatrix.get(pdIdx));
            temp_y = LinearAlgebra.doFilter(temp_y, this.filterMatrix.get(pdIdx));
            // temp_xy = temp_x * temp_y, 只有一个元素
            List<BigInteger> temp_xy = LinearAlgebra.dotProduct(temp_x, temp_y);
            if(temp_xy.size() > 1) {
                throw new RuntimeException("temp_xy should only one element");
            }
            temp_xy_list.add(temp_xy.getFirst());
            // Compute [Delta_z] according to the paper
            // [Delta_z] = [c] + [lambda_z]
//            List<Share> Delta_z_shr = LinearAlgebra.addSharesVec(c_shr, lambda_share_list);
//            List<Share> Delta_z_shr = c_shr_matrix.get(pdIdx);//只有一个元素在这个数组中
            List<Share> c_shr = c_shr_matrix.get(pdIdx);
            c_shr = LinearAlgebra.doFilter(c_shr, this.filterMatrix.get(pdIdx));
//            Share sum_of_c_shr = c_shr.stream().reduce((s1, s2)->{return s1.add(s2);}).get();
            Share sum_of_c_shr = LinearAlgebra.reduceSum(c_shr);
            List<Share> Delta_z_shr = List.of(sum_of_c_shr);//只有一个元素在这个数组中
            // [Delta_z] -= [a] * temp_y
            Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr,
                    LinearAlgebra.dotProduct2(a_shr_matrix.get(pdIdx), temp_y));
            // [Delta_z] -= temp_x * [b]
            Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr,
                    LinearAlgebra.dotProduct2(b_shr_matrix.get(pdIdx), temp_x));
            if(Delta_z_shr.size() > 1) {
                throw new RuntimeException("Delta_z_shr should have only one element");
            }
            Delta_z_share_list.add(Delta_z_shr.getFirst());
        }

        //To scale the result of dot product
        if(this.scaling != null){
            for(int i = 0; i < Delta_z_share_list.size(); i++){
                Share s = Delta_z_share_list.get(i);
                Delta_z_share_list.set(i, s.multiply(this.scaling));
                temp_xy_list.set(i, temp_xy_list.get(i).multiply(this.scaling));
            }
        }

        /******************** start the division **********************************/
        BigInteger two2l_sigma = BigInteger.TWO.pow(OfflineDivisionGate.l + OfflineDivisionGate.sigma);
        List<BigInteger> two2l_sigma_list = new ArrayList<>();
        for(int i = 0; i < numDotProd; i++){
            two2l_sigma_list.add(two2l_sigma);
        }
        List<Share> shares_h = LinearAlgebra.elemWiseMultiply2(r1_list, two2l_sigma_list);
        shares_h = LinearAlgebra.addSharesVec(r_list, shares_h);
        shares_h = LinearAlgebra.elemWiseMultiply2(shares_h, divisors);

//        List<Share> shares_x = LinearAlgebra.subtractVec2(this.firstGate().Delta_clear_list, this.firstGate().lambda_share_list);
        List<Share> shares_x = Delta_z_share_list; // Newly-added

        List<Share> shares_z = LinearAlgebra.elemWiseMultiply2(shares_x, two2l_sigma_list);
        shares_z = LinearAlgebra.addSharesVec(shares_z, shares_h);
        shares_z = LinearAlgebra.addSharesVec(shares_z, r2_list);
        //open z
        edgeServer.sendToKing(shares_z);
        if(edgeServer.isKing()){
            List<Object> objs = edgeServer.kingReadFromAll();
            List<BigInteger> z_clear_list = Tool.openShares2Values(numDotProd, objs);

            //Newly-added. temp_xy is not shared with other servers
            z_clear_list = LinearAlgebra.addBigIntVec(z_clear_list,
                    LinearAlgebra.elemWiseMultiply(two2l_sigma_list, temp_xy_list));

            edgeServer.kingSendToAll(z_clear_list);
        }
        List<BigInteger> z_clear_list = (List<BigInteger>)edgeServer.readFromKing();

        // Delta_clear = \lfloor z/(2^{l+sigma} \cdot d)  \rfloor
        List<BigInteger> d_temp = LinearAlgebra.elemWiseMultiply(two2l_sigma_list, divisors);
        this.Delta_clear_list = new ArrayList<>();
        for(int i = 0; i < numDotProd; i++){
            this.Delta_clear_list.add(z_clear_list.get(i).divide(d_temp.get(i)));
        }
        this.lambda_share_list = this.r1_list;
    }
}
