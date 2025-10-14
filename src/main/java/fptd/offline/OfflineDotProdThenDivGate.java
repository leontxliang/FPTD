package fptd.offline;

import fptd.Share;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OfflineDotProdThenDivGate extends OfflineGate {

    private List<OfflineGate> xGates = new ArrayList<>(); // The i-th xGate dot-product the i-th yGate
    private List<OfflineGate> yGates = new ArrayList<>();
//    private FakeOutputGate divisorGate;
    private int num_dot_prod = -1; //计算dot product的个数

    /**
     * 有dim次内积，每一次内积的结果是一个数字，因此，需要使用combine Gate来合并这个内积的output gates，以升维
     * @param inputGates include X gates, then Y gates, finally on divisorGate,
     *                   e.g., gateX1, gateX2, ..., gateX_dim, gateY1, gateY2, ..., gateY_dim, divisorGate
     */
    public OfflineDotProdThenDivGate(List<OfflineGate> inputGates) {
        super(inputGates.toArray(new OfflineGate[inputGates.size()]));
        if(inputGates.size() < 3) {
            throw new IllegalArgumentException("wrong number of inputs");
        }
//        this.divisorGate = (FakeOutputGate) inputGates.getLast();

//        if(inputGates.size() != this.dim * 2 + 1){
//            throw new IllegalArgumentException("wrong number of inputs");
//        }
        this.num_dot_prod = (inputGates.size()-1) / 2;
        for(int i = 0; i < num_dot_prod; i++){
            this.xGates.add(inputGates.get(i));
        }
        for(int i = num_dot_prod; i < inputGates.size()-1; i++){
            this.yGates.add(inputGates.get(i));
        }
        this.dim = num_dot_prod;
    }

    @Override
    void doRunOffline() {
        final int numDotProd = num_dot_prod;
        for(int dpIdx = 0; dpIdx < numDotProd; dpIdx++) {
            List<BigInteger> a_clear_list = new ArrayList<>(firstGate().dim);
            List<BigInteger> b_clear_list = new ArrayList<>(firstGate().dim);
            List<BigInteger> c_clear_list = new ArrayList<>(firstGate().dim);

            for (int i = 0; i < firstGate().dim; i++) {
                BigInteger a = Tool.getRand(64);
                BigInteger b = Tool.getRand(64);
                a_clear_list.add(a);
                b_clear_list.add(b);
                c_clear_list.add(a.multiply(b));
            }
//            c_clear_list = LinearAlgebra.dotProduct(a_clear_list, b_clear_list);

            List<List<Share>> a_shares = fakeParty.generateAllPartiesShares(a_clear_list);
            List<List<Share>> b_shares = fakeParty.generateAllPartiesShares(b_clear_list);
            List<List<Share>> c_shares = fakeParty.generateAllPartiesShares(c_clear_list);
            // $\delta_x = a - \lambda_x$, $\delta_y = b - \lambda_y$
            List<BigInteger> delta_x_clear = LinearAlgebra.subtractBigIntVec(a_clear_list, this.xGates.get(dpIdx).lambda_clear_list);
            List<BigInteger> delta_y_clear = LinearAlgebra.subtractBigIntVec(b_clear_list, this.yGates.get(dpIdx).lambda_clear_list);
            //Write all data to files
            fakeParty.writeSharesToAllParties(a_shares);
            fakeParty.writeSharesToAllParties(b_shares);
            fakeParty.writeSharesToAllParties(c_shares);
            fakeParty.writeClearToAllParties(delta_x_clear);
            fakeParty.writeClearToAllParties(delta_y_clear);
        }


        {   //For division
            Random rand = new Random();
            List<BigInteger> r_list = new ArrayList<>();
            List<BigInteger> r1_list = new ArrayList<>();
            List<BigInteger> r2_list = new ArrayList<>();
            for(int i = 0; i < num_dot_prod; i++) {
                BigInteger r = new BigInteger(OfflineDivisionGate.l + OfflineDivisionGate.sigma, rand);
                BigInteger r1 = new BigInteger(OfflineDivisionGate.e + OfflineDivisionGate.sigma, rand);
                BigInteger r2 = new BigInteger(OfflineDivisionGate.l + OfflineDivisionGate.sigma, rand);
                r_list.add(r);
                r1_list.add(r1);
                r2_list.add(r2);
            }
            List<List<Share>> shares_r = this.fakeParty.generateAllPartiesShares(r_list);
            List<List<Share>> shares_r1 = this.fakeParty.generateAllPartiesShares(r1_list);
            List<List<Share>> shares_r2 = this.fakeParty.generateAllPartiesShares(r2_list);
            this.fakeParty.writeSharesToAllParties(shares_r);
            this.fakeParty.writeSharesToAllParties(shares_r1);
            this.fakeParty.writeSharesToAllParties(shares_r2);

            this.lambda_clear_list = r1_list;
            this.lambda_shr_matrix = shares_r1;
        }
    }
}
