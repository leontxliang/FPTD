package fptd.protocols;

import static fptd.Params.P;

import fptd.Share;
import fptd.offline.OfflineDivisionGate;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * privDiv([x], d)
 * 生成随机数的秘密份额[r][r1][r2]
 * [h] = ([r] + 2^{l + sigma} \cdot [r1])\cdot d
 * [z] = 2^{l + sigma}\cdot x + [h] + [r2]
 * Open z
 * Delta_clear = \lfloor z/(2^{l+sigma} \cdot d)  \rfloor
 *
 *
 * To open x/d, compute (Delta_clear - r1)
 */
public class DivisionGate extends Gate {

    private List<BigInteger> divisors;
    private OutputGate divisorGate; //Get the divisors from this gate

    private List<Share> r_list;
    private List<Share> r1_list;
    private List<Share> r2_list;


    public DivisionGate(Gate inputX, List<BigInteger> divisors) {
        super(inputX, null);

        this.divisors = divisors;
        assert required0();

        this.dim = inputX.dim;
    }

    private boolean required0() {
        // Check the requirements
        for (BigInteger d : divisors) {
            if (OfflineDivisionGate.e + 2 * (OfflineDivisionGate.l + OfflineDivisionGate.sigma) >= P.bitLength()) {
                System.out.println("(e + 2 * (l + sigma) < len(bin(PRIME)))"
                        + (OfflineDivisionGate.e + 2 * (OfflineDivisionGate.l + OfflineDivisionGate.sigma))
                        + ", len(P)=" + P.bitLength());
                return false;
            }
            if (d.bitLength() >= OfflineDivisionGate.l) {
                System.out.println("0 < d < 2^l, Bit(b) = " + d.bitLength());
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param divisorGate 除数
     * @param inputX 被除数
     */
    public DivisionGate(Gate inputX, OutputGate divisorGate) {
        super(inputX, divisorGate);
        this.dim = inputX.dim;
        this.divisorGate = divisorGate;
    }


    @Override
    void doReadOfflineFromFile() {
        r_list = edgeServer.readRandShares(dim);
        r1_list = edgeServer.readRandShares(dim);
        r2_list = edgeServer.readRandShares(dim);
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

    @Override
    void doRunOnline() {
        assert required();

        if (this.divisorGate != null) {
            List<BigInteger> outputValues = this.divisorGate.getOutputValues();
            //copy第一个元素填充其他位置，使其长度为dim
            for (int i = outputValues.size(); i < this.firstGate().dim; i++) {
                outputValues.add(outputValues.getFirst());
            }
            this.divisors = outputValues;
        }

        BigInteger two2l_sigma = BigInteger.TWO.pow(OfflineDivisionGate.l + OfflineDivisionGate.sigma);
        List<BigInteger> two2l_sigma_list = new ArrayList<>();
        for (int i = 0; i < this.firstGate().dim; i++) {
            two2l_sigma_list.add(two2l_sigma);
        }
        List<Share> shares_h = LinearAlgebra.elemWiseMultiply2(r1_list, two2l_sigma_list);
        shares_h = LinearAlgebra.addSharesVec(r_list, shares_h);
        shares_h = LinearAlgebra.elemWiseMultiply2(shares_h, divisors);

        List<Share> shares_x = LinearAlgebra.subtractVec2(this.firstGate().Delta_clear_list,
                this.firstGate().lambda_share_list);
        List<Share> shares_z = LinearAlgebra.elemWiseMultiply2(shares_x, two2l_sigma_list);
        shares_z = LinearAlgebra.addSharesVec(shares_z, shares_h);
        shares_z = LinearAlgebra.addSharesVec(shares_z, r2_list);
        //open z
        edgeServer.sendToKing(shares_z);
        if (edgeServer.isKing()) {
            List<Object> objs = edgeServer.kingReadFromAll();
            List<BigInteger> z_clear_list = Tool.openShares2Values(dim, objs);
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
}











