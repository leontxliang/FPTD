package fptd.offline;

import fptd.Share;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * inputX 的维度为m1，inputY的维度为m2，那么combine之后的Gate的维度为m1+m2
 */
public class OfflineCombinationGate extends OfflineGate {

    public OfflineCombinationGate(OfflineGate... inputGates) {
        super(inputGates);
        this.dim = 0;
        for(OfflineGate gate : inputGates) {
            this.dim = gate.dim + dim;
        }
    }

    @Override
    void doRunOffline() {
        List<List<Share>> shares = new ArrayList<>();
        for(int i = 0; i < this.firstGate().lambda_shr_matrix.size(); i++) {
            shares.add(new ArrayList<>());
        }
        for(int i = 0; i < shares.size(); i++) {
            List<Share> shares1 = this.firstGate().lambda_shr_matrix.get(i);
            for(int j = 0; j < shares1.size(); j++) {
                shares.get(i).add(shares1.get(j));//copy
            }
        }
        for(int i = 0; i < shares.size(); i++) {
//            List<Share> shares1 = inputY.lambda_shr_matrix.get(i);
            for(int gateIdx = 1; gateIdx < this.inputGates.length; gateIdx++) {//for the gateIdx gate
                List<Share> shares1 = this.inputGates[gateIdx].lambda_shr_matrix.get(i);
                for(int j = 0; j < shares1.size(); j++) {
                    shares.get(i).add(shares1.get(j));//copy
                }
            }
        }
        this.lambda_shr_matrix = shares;

        List<BigInteger> new_lambda_clear_list = new ArrayList<>();
        firstGate().lambda_clear_list.forEach(x->{
            new_lambda_clear_list.add(x);
        });

        for(int gateIdx = 1; gateIdx < this.inputGates.length; gateIdx++) {
            this.inputGates[gateIdx].lambda_clear_list.forEach(x->{
                new_lambda_clear_list.add(x);
            });
        }

        this.lambda_clear_list = new_lambda_clear_list;
    }
}











