package fptd.protocols;

import fptd.utils.LinearAlgebra;
import java.math.BigInteger;
import java.util.List;

public class AddConstantGate extends Gate {
    private List<BigInteger> constants;

    public AddConstantGate(Gate inputX, List<BigInteger> constants) {
        super(inputX, null);
        this.dim = inputX.dim;
        this.constants = constants;
    }

    @Override
    void doReadOfflineFromFile() {

    }

    @Override
    void doRunOnline() {
        this.lambda_share_list = this.firstGate().lambda_share_list;
        this.Delta_clear_list = LinearAlgebra.addBigIntVec(this.firstGate().Delta_clear_list, constants);
    }
}
