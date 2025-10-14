package fptd.protocols;

import fptd.Share;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;

import java.math.BigInteger;
import java.util.List;

public class ElemWiseMultiplyGate extends Gate {

    private List<Share> a_shr;
    private List<Share> b_shr;
    private List<Share> c_shr;
    private List<BigInteger> delta_x_clear_list;
    private List<BigInteger> delta_y_clear_list;

    public ElemWiseMultiplyGate(Gate inputX, Gate inputY) {
        super(inputX, inputY);
        if(inputX.getDim() != inputY.getDim()) {
            throw new IllegalArgumentException("Input dimensions do not match");
        }
    }

    @Override
    void doReadOfflineFromFile() {
        a_shr = edgeServer.readRandShares(dim);
        b_shr = edgeServer.readRandShares(dim);
        c_shr = edgeServer.readRandShares(dim);
        this.lambda_share_list = edgeServer.readRandShares(dim);
        this.delta_x_clear_list = edgeServer.readClear(dim);
        this.delta_y_clear_list = edgeServer.readClear(dim);
    }

    @Override
    void doRunOnline() {
        // temp_x = $\Delta_x + \delta_x$
        List<BigInteger> temp_x = LinearAlgebra.addBigIntVec(this.firstGate().Delta_clear_list, delta_x_clear_list);
        // temp_y = $\Delta_y + \delta_y$
        List<BigInteger> temp_y = LinearAlgebra.addBigIntVec(secondGate().Delta_clear_list, delta_y_clear_list);
        // temp_xy = temp_x * temp_y
        List<BigInteger> temp_xy = LinearAlgebra.elemWiseMultiply(temp_x, temp_y);
        // Compute [Delta_z] according to the paper
        // [Delta_z] = [c] + [lambda_z]
        List<Share> Delta_z_shr = LinearAlgebra.addSharesVec(c_shr, lambda_share_list);
        // [Delta_z] -= [a] * temp_y
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.elemWiseMultiply2(a_shr, temp_y));
        // [Delta_z] -= temp_x * [b]
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.elemWiseMultiply2(b_shr, temp_x));

        //To open Delta_z in the clear
        edgeServer.sendToKing(Delta_z_shr);
        if(edgeServer.isKing()){
            List<Object> shares = edgeServer.kingReadFromAll();
            List<BigInteger> values = Tool.openShares2Values(dim, shares);

            values = LinearAlgebra.addBigIntVec(values, temp_xy); // temp_xy is not shared with other servers

            //Let other servers know Delta_clear_list, i.e., values
            edgeServer.kingSendToAll(values);
        }
        this.Delta_clear_list = (List<BigInteger>)edgeServer.readFromKing();
    }

}
