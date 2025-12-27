package fptd.offline;

import fptd.Share;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OfflineScalingGate extends OfflineGate {

    private BigInteger roundingFactor;

    public OfflineScalingGate(OfflineGate inputX, BigInteger roundingFactor) {
        super(inputX, null);
        this.roundingFactor = roundingFactor;
        this.dim = inputX.dim;
    }

    @Override
    void doRunOffline() {
        this.lambda_clear_list = new ArrayList<>();
        this.lambda_shr_matrix = new ArrayList<>();

        for (BigInteger v : firstGate().lambda_clear_list) {
            this.lambda_clear_list.add(v.multiply(this.roundingFactor));
        }
        for (int i = 0; i < firstGate().lambda_shr_matrix.size(); i++) {
            List<Share> shares = firstGate().lambda_shr_matrix.get(i);
            List<Share> newShareList = new ArrayList<>();
            for (int j = 0; j < shares.size(); j++) {
                Share share = shares.get(j);
                BigInteger newValue = share.getShr().multiply(this.roundingFactor);
                newShareList.add(share.setValue(newValue));
            }
            this.lambda_shr_matrix.add(newShareList);
        }
        this.fakeParty.writeSharesToAllParties(this.lambda_shr_matrix);
    }
}
