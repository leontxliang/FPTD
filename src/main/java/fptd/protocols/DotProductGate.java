package fptd.protocols;

import fptd.Share;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DotProductGate extends Gate {

    private List<Share> a_shr;
    private List<Share> b_shr;
    private List<Share> c_shr;
    private List<BigInteger> delta_x_clear_list;
    private List<BigInteger> delta_y_clear_list;
    private List<BigInteger> roundingFactorsToEliminate = new ArrayList<>();

    public DotProductGate(Gate inputX, Gate inputY) {
        super(inputX, inputY);
        if (inputX.getDim() != inputY.getDim()) {
            throw new IllegalArgumentException("Input dimensions don't match");
        }
        this.dim = 1;
    }

    public DotProductGate(Gate inputX, Gate inputY, List<BigInteger> roundingFactorsToEliminate) {
        this(inputX, inputY);
        this.roundingFactorsToEliminate = roundingFactorsToEliminate;
        this.dim = 1;
    }

    @Override
    void doReadOfflineFromFile() {
        a_shr = edgeServer.readRandShares(this.firstGate().dim);
        b_shr = edgeServer.readRandShares(this.firstGate().dim);
        c_shr = edgeServer.readRandShares(1);
        this.lambda_share_list = edgeServer.readRandShares(1);
        this.delta_x_clear_list = edgeServer.readClear(this.firstGate().dim);
        this.delta_y_clear_list = edgeServer.readClear(this.firstGate().dim);
    }

    @Override
    void doRunOnline() {
        // temp_x = $\Delta_x + \delta_x$
        List<BigInteger> temp_x = LinearAlgebra.addBigIntVec(this.firstGate().Delta_clear_list, delta_x_clear_list);
        // temp_y = $\Delta_y + \delta_y$
        List<BigInteger> temp_y = LinearAlgebra.addBigIntVec(secondGate().Delta_clear_list, delta_y_clear_list);
        // temp_xy = temp_x * temp_y, 只有一个元素
        List<BigInteger> temp_xy = LinearAlgebra.dotProduct(temp_x, temp_y);
        // Compute [Delta_z] according to the paper
        // [Delta_z] = [c] + [lambda_z]
        List<Share> Delta_z_shr = LinearAlgebra.addSharesVec(c_shr, lambda_share_list);
        // [Delta_z] -= [a] * temp_y
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.dotProduct2(a_shr, temp_y));
        // [Delta_z] -= temp_x * [b]
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.dotProduct2(b_shr, temp_x));

        //To open Delta_z in the clear
        edgeServer.sendToKing(Delta_z_shr);
        if (edgeServer.isKing()) {
            List<Object> shares = edgeServer.kingReadFromAll();
            List<BigInteger> values = Tool.openShares2Values(1, shares);

            values = LinearAlgebra.addBigIntVec(values, temp_xy); // temp_xy is not shared with other servers

            //Let other servers know Delta_clear_list, i.e., values
            edgeServer.kingSendToAll(values);
        }
        //To eliminate the rounding factors
        BigInteger roundingFactorAll = BigInteger.ONE;
        for (BigInteger rounding : roundingFactorsToEliminate) {
            roundingFactorAll = roundingFactorAll.multiply(rounding);
        }
        List<BigInteger> DeltaTempList = (List<BigInteger>) edgeServer.readFromKing();
        this.Delta_clear_list = new ArrayList<>();
        for (BigInteger delta : DeltaTempList) {
            this.Delta_clear_list.add(delta.divide(roundingFactorAll));
        }
    }
}
