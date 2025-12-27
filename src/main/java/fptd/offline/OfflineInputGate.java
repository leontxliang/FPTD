package fptd.offline;

import java.math.BigInteger;
import java.util.Random;

public class OfflineInputGate extends OfflineGate {

    private int owner_id;

    public OfflineInputGate(FakeParty fakeParty, int dim, int owner_id) {
        super(fakeParty, dim);
        this.owner_id = owner_id;
    }

    @Override
    void doRunOffline() {
        int size = this.dim;
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            BigInteger r = new BigInteger(64, rand);
//            BigInteger r = BigInteger.valueOf(88888888 * (i + 1));
//            System.out.println("r = " + r);
            this.lambda_clear_list.add(r);
        }
        this.lambda_shr_matrix = this.fakeParty.generateAllPartiesShares(lambda_clear_list);

//        this.fakeParty.writeClearToIthParty(this.lambda_clear_list, owner_id);
        this.fakeParty.writeSharesToAllParties(this.lambda_shr_matrix);
    }
}
