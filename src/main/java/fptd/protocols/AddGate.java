package fptd.protocols;

import fptd.utils.LinearAlgebra;

public class AddGate extends Gate{
//    private boolean isUseOfflineRand = true;

    public AddGate(Gate ... inputGates) {
        super(inputGates);
        for(int i = 1; i < inputGates.length; i++) {
            if(inputGates[i-1].dim != inputGates[i].dim) {
                throw new IllegalArgumentException("Input dimensions don't match");
            }
        }
        this.dim = inputGates[0].dim;
    }

    @Override
    void doReadOfflineFromFile() {
        int size = this.dim;
        this.lambda_share_list = this.edgeServer.readRandShares(size);
    }

    @Override
    void doRunOnline() {
//        this.Delta_clear_list = LinearAlgebra.addBigIntVec(this.firstGate().Delta_clear_list, this.inputY.Delta_clear_list);

        this.Delta_clear_list = this.firstGate().Delta_clear_list;
        for(int i = 1; i < inputGates.length; i++) {
            this.Delta_clear_list = LinearAlgebra.addBigIntVec(this.Delta_clear_list, inputGates[i].Delta_clear_list);
        }

    }
}
