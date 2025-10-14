package fptd.offline;

import fptd.utils.LinearAlgebra;

public class OfflineAddGate extends OfflineGate {

    public OfflineAddGate(OfflineGate... inputGates) {
        super(inputGates);
        for(int i = 1; i < inputGates.length; i++) {
            if(inputGates[i-1].dim != inputGates[i].dim) {
                throw new IllegalArgumentException("Input dimensions don't match");
            }
        }
        this.dim = inputGates[0].dim;
    }

    @Override
    void doRunOffline() {
        this.lambda_shr_matrix = this.firstGate().lambda_shr_matrix;
        for(int i = 1; i < this.inputGates.length; i++){
            this.lambda_shr_matrix = LinearAlgebra.addShareMatrix(this.lambda_shr_matrix, this.getIthGate(i).lambda_shr_matrix);
        }

        // Write the lambda values to the output files
        this.fakeParty.writeSharesToAllParties(this.lambda_shr_matrix);

//        this.lambda_clear_list = LinearAlgebra.addBigIntVec(inputX.lambda_clear_list, inputY.lambda_clear_list);

        this.lambda_clear_list = this.firstGate().lambda_clear_list;
        for(int i = 1; i < this.inputGates.length; i++){
            this.lambda_clear_list = LinearAlgebra.addBigIntVec(this.lambda_clear_list, this.getIthGate(i).lambda_clear_list);
        }
    }
}
