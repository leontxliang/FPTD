package fptd.protocols;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Scale the data via the rounding factors
 */
public class ScalingGate extends Gate {
    private BigInteger roundingFactor;

    public ScalingGate(Gate inputX, BigInteger roundingFactor) {
        super(inputX, null);
        this.dim = inputX.dim;
        this.roundingFactor = roundingFactor;
    }

    @Override
    void doReadOfflineFromFile() {
//        this.lambda_share_list = inputX.getLambda_share_list();
        this.lambda_share_list = this.edgeServer.readRandShares(dim);
    }

    @Override
    void doRunOnline() {
        this.Delta_clear_list = new ArrayList<>();
        for(BigInteger Delta: this.firstGate().Delta_clear_list){
            this.Delta_clear_list.add(Delta.multiply(this.roundingFactor));
        }
    }
}
