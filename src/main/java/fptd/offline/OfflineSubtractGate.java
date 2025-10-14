package fptd.offline;

import fptd.Params;
import fptd.utils.LinearAlgebra;

public class OfflineSubtractGate extends OfflineGate {

    public OfflineSubtractGate(OfflineGate inputX, OfflineGate inputY) {
        super(inputX, inputY);
        if(inputX.dim != inputY.dim) {
            throw new IllegalArgumentException("Input dimensions don't match");
        }
        this.dim = inputX.dim;
    }

    @Override
    void doRunOffline() {
        for(int i = 0; i < Params.NUM_SERVER; i++){
            this.lambda_shr_matrix = LinearAlgebra.subtractShareMatrix(firstGate().lambda_shr_matrix, secondGate().lambda_shr_matrix);
        }
        // Write the lambda values to the output files
        this.fakeParty.writeSharesToAllParties(this.lambda_shr_matrix);
        this.lambda_clear_list = LinearAlgebra.subtractBigIntVec(firstGate().lambda_clear_list, secondGate().lambda_clear_list);
    }
}
