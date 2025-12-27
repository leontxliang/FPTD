package fptd.protocols;

import static fptd.Params.P;

import fptd.Share;
import fptd.offline.OfflineDivisionGate;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ElemMulThenDivGate extends Gate {

    //Fields for multiply
    private List<Share> a_shr;
    private List<Share> b_shr;
    private List<Share> c_shr;
    private List<BigInteger> delta_x_clear_list;
    private List<BigInteger> delta_y_clear_list;

    //Fields for division
    private OutputGate divisorGate; //Get the divisors from this gate
    private List<Share> r_list;
    private List<Share> r1_list;
    private List<Share> r2_list;
    private List<BigInteger> divisors = null;

    public ElemMulThenDivGate(Gate inputX, Gate inputY, OutputGate divisorGate) {
        super(inputX, inputY, divisorGate);
        if (inputX.dim != inputY.dim || inputX.dim != divisorGate.dim) {
            throw new RuntimeException("Dims don't match");
        }
        this.divisorGate = divisorGate;
        this.dim = inputX.dim;
    }

    public ElemMulThenDivGate(Gate inputX, Gate inputY, List<BigInteger> divisors) {
        super(inputX, inputY);
        if (inputX.dim != inputY.dim || inputX.dim != divisorGate.dim) {
            throw new RuntimeException("Dims don't match");
        }
        this.divisors = divisors;
        this.dim = inputX.dim;
    }

    @Override
    void doReadOfflineFromFile() {
        //randomness for multiply
        a_shr = edgeServer.readRandShares(dim);
        b_shr = edgeServer.readRandShares(dim);
        c_shr = edgeServer.readRandShares(dim);
//        this.lambda_share_list = edgeServer.readRandShares(dim);
        this.delta_x_clear_list = edgeServer.readClear(dim);
        this.delta_y_clear_list = edgeServer.readClear(dim);

        //randomness for division
        r_list = edgeServer.readRandShares(dim);
        r1_list = edgeServer.readRandShares(dim);
        r2_list = edgeServer.readRandShares(dim);
    }

    @Override
    void doRunOnline() {
        assert required() : "The division requirement is not met.";
        if (this.divisorGate != null) {
            List<BigInteger> outputValues = this.divisorGate.getOutputValues();
            assert required(outputValues) : "The division requirement is not met.";
            //Copy第一个元素填充其他位置，使其长度为dim
            for (int i = outputValues.size(); i < this.firstGate().dim; i++) {
                outputValues.add(outputValues.getFirst());
//                System.out.println("divisor in DivGate = " + outputValues.getFirst());
            }
            this.divisors = outputValues;
        }

        /******************** start the multiplication **********************************/
        // temp_x = $\Delta_x + \delta_x$
        List<BigInteger> temp_x = LinearAlgebra.addBigIntVec(this.firstGate().Delta_clear_list, delta_x_clear_list);
        // temp_y = $\Delta_y + \delta_y$
        List<BigInteger> temp_y = LinearAlgebra.addBigIntVec(secondGate().Delta_clear_list, delta_y_clear_list);
        // temp_xy = temp_x * temp_y
        List<BigInteger> temp_xy = LinearAlgebra.elemWiseMultiply(temp_x, temp_y);
        // Compute [Delta_z] according to the paper
        // [Delta_z] = [c] + [lambda_z]

//        List<Share> Delta_z_shr = LinearAlgebra.addSharesVec(c_shr, lambda_share_list);
        List<Share> Delta_z_shr = c_shr; //Newly-added

        // [Delta_z] -= [a] * temp_y
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.elemWiseMultiply2(a_shr, temp_y));
        // [Delta_z] -= temp_x * [b]
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.elemWiseMultiply2(b_shr, temp_x));

        /******************** start the division **********************************/
        BigInteger two2l_sigma = BigInteger.TWO.pow(OfflineDivisionGate.l + OfflineDivisionGate.sigma);
        List<BigInteger> two2l_sigma_list = new ArrayList<>();
        for (int i = 0; i < this.firstGate().dim; i++) {
            two2l_sigma_list.add(two2l_sigma);
        }
        List<Share> shares_h = LinearAlgebra.elemWiseMultiply2(r1_list, two2l_sigma_list);
        shares_h = LinearAlgebra.addSharesVec(r_list, shares_h);
        shares_h = LinearAlgebra.elemWiseMultiply2(shares_h, divisors);

//        List<Share> shares_x = LinearAlgebra.subtractVec2(this.firstGate().Delta_clear_list, this.firstGate().lambda_share_list);
        List<Share> shares_x = Delta_z_shr; // Newly-added

        List<Share> shares_z = LinearAlgebra.elemWiseMultiply2(shares_x, two2l_sigma_list);
        shares_z = LinearAlgebra.addSharesVec(shares_z, shares_h);
        shares_z = LinearAlgebra.addSharesVec(shares_z, r2_list);
        //open z
        edgeServer.sendToKing(shares_z);
        if (edgeServer.isKing()) {
            List<Object> objs = edgeServer.kingReadFromAll();
            List<BigInteger> z_clear_list = Tool.openShares2Values(dim, objs);

            //Newly-added. temp_xy is not shared with other servers
            z_clear_list = LinearAlgebra.addBigIntVec(z_clear_list,
                    LinearAlgebra.elemWiseMultiply(two2l_sigma_list, temp_xy));

            edgeServer.kingSendToAll(z_clear_list);
        }
        List<BigInteger> z_clear_list = (List<BigInteger>) edgeServer.readFromKing();

        // Delta_clear = \lfloor z/(2^{l+sigma} \cdot d)  \rfloor
        List<BigInteger> d_temp = LinearAlgebra.elemWiseMultiply(two2l_sigma_list, divisors);
        this.Delta_clear_list = new ArrayList<>();
        for (int i = 0; i < this.firstGate().dim; i++) {
            this.Delta_clear_list.add(z_clear_list.get(i).divide(d_temp.get(i)));
        }
        this.lambda_share_list = this.r1_list;
    }

    private boolean required(List<BigInteger> divisors) {
        // Check the requirements
        for (BigInteger d : divisors) {
            if (OfflineDivisionGate.e + 2 * (OfflineDivisionGate.l + OfflineDivisionGate.sigma) >= P.bitLength()) {
                System.out.println("(e + 2 * (l + sigma) < len(bin(PRIME)))"
                        + (OfflineDivisionGate.e + 2 * (OfflineDivisionGate.l + OfflineDivisionGate.sigma))
                        + ", len(P)=" + P.bitLength());
                return false;
            }
            if (d.bitLength() >= OfflineDivisionGate.l) {
                System.out.println("0 < d < 2^l");
                return false;
            }
        }
        return true;
    }

    private boolean required() {
        //Check the requirement
        edgeServer.sendToKing(
                LinearAlgebra.subtractVec2(this.firstGate().Delta_clear_list, this.firstGate().lambda_share_list));
        if (edgeServer.isKing()) {
            List<Object> objs = edgeServer.kingReadFromAll();
            List<BigInteger> x_list = Tool.openShares2Values(this.firstGate().dim, objs);
            for (BigInteger x : x_list) {
                if (x.bitLength() >= OfflineDivisionGate.e) {
                    System.out.println("0 <= x.bitlen < e. bit(x)=" + x.bitLength() + ", e = " + OfflineDivisionGate.e);
                    return false;
                }
            }
        }
        return true;
    }
}
