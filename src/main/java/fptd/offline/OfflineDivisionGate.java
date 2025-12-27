package fptd.offline;

import fptd.Share;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OfflineDivisionGate extends OfflineGate {
//    public static final int l = 40;
//    public static final int e = 100;
//    public static final int sigma = 150;

    public static final int l = 64;
    public static final int e = 90;
    public static final int sigma = 64;

    public OfflineDivisionGate(OfflineGate inputX) {
        super(inputX, null);
        this.dim = inputX.dim;
    }

    /**
     * @param divisorGate 除数
     * @param inputX 被除数
     */
    public OfflineDivisionGate(OfflineGate inputX, OfflineOutputGate divisorGate) {
        super(inputX, divisorGate);
        this.dim = inputX.dim;
    }

    @Override
    void doRunOffline() {
        Random rand = new Random();
        List<BigInteger> r_list = new ArrayList<>();
        List<BigInteger> r1_list = new ArrayList<>();
        List<BigInteger> r2_list = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            BigInteger r = new BigInteger(l + sigma, rand);
            BigInteger r1 = new BigInteger(e + sigma, rand);
            BigInteger r2 = new BigInteger(l + sigma, rand);
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