package fptd.protocols;

import fptd.utils.LinearAlgebra;

import java.util.List;

public class SubtractGate extends Gate {
    private List<List<Boolean>> missingValueMatrix = null;

    public SubtractGate(Gate inputX, Gate inputY) {
        super(inputX, inputY);
        if(inputX.getDim() != inputY.getDim() ){
            throw new IllegalArgumentException("Input dimensions do not match");
        }
        this.setDim(inputX.getDim());
    }

    public SubtractGate(Gate inputX, Gate inputY, List<List<Boolean>> missingValueMatrix){
        this(inputX, inputY);
        this.missingValueMatrix = missingValueMatrix;
    }

    @Override
    void doReadOfflineFromFile() {
        int size = this.dim;
        this.lambda_share_list = this.edgeServer.readRandShares(size);
    }

    @Override
    void doRunOnline() {
        this.Delta_clear_list = LinearAlgebra.subtractBigIntVec(
                this.firstGate().Delta_clear_list, this.secondGate().Delta_clear_list);
    }
}
