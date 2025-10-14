package fptd.protocols;

import fptd.Share;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Combine two gate into one gate
 */
public class CombinationGate extends Gate {

    public CombinationGate(Gate... inputGates) {
        super(inputGates);
        this.dim = 0;
        for(Gate gate : inputGates) {
            this.dim = gate.dim + dim;
        }
    }

    @Override
    void doReadOfflineFromFile() {
    }

    @Override
    void doRunOnline() {
        List<Share> new_lambda_share_list = new ArrayList<>();
        this.firstGate().lambda_share_list.forEach(x->{
            new_lambda_share_list.add(x);
        });
        List<BigInteger> new_Delta_clear_list = new ArrayList<>();
        this.firstGate().Delta_clear_list.forEach(x->{
            new_Delta_clear_list.add(x);
        });
        for(int gateIdx = 1; gateIdx < this.inputGates.length; gateIdx++) {
            inputGates[gateIdx].lambda_share_list.forEach(x->{
                new_lambda_share_list.add(x);
            });
            inputGates[gateIdx].Delta_clear_list.forEach(x->{
                new_Delta_clear_list.add(x);
            });
        }

        this.lambda_share_list = new_lambda_share_list;
        this.Delta_clear_list = new_Delta_clear_list;
    }
}
