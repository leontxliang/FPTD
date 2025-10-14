package fptd.protocols;

import fptd.Params;
import fptd.Share;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * perform dot product operation with a filter
 * to ignore the values in some dimensions
 *
 * 根据inputX和inputY的Delta_clear_list中的元素是否为null来自动构建filter，只有对应的元素都不为null，那么filter才为true
 */
public class DotProdWithFilterGate extends Gate {
    private List<Share> a_shr;
    private List<Share> b_shr;
    private List<Share> c_shr;
    private List<BigInteger> delta_x_clear_list;
    private List<BigInteger> delta_y_clear_list;
    private List<Boolean> filter; //if 0, the value in this dimension will be ignored in the dot product.

    /**
     *
     * @param inputX
     * @param inputY
     */
    public DotProdWithFilterGate(Gate inputX, Gate inputY) {
        super(inputX, inputY);
        if(inputX.getDim() != inputY.getDim()) {
            throw new IllegalArgumentException("Input dimensions don't match");
        }
        this.dim = 1;
    }

    @Override
    void doReadOfflineFromFile() {
        a_shr = edgeServer.readRandShares(this.firstGate().dim);
        b_shr = edgeServer.readRandShares(this.firstGate().dim);
        c_shr = edgeServer.readRandShares(this.firstGate().dim);
        this.lambda_share_list = edgeServer.readRandShares(1);
        this.delta_x_clear_list = edgeServer.readClear(this.firstGate().dim);
        this.delta_y_clear_list = edgeServer.readClear(this.firstGate().dim);
    }

    @Override
    void doRunOnline() {
        //initialize the filter
        this.filter = new ArrayList<>();
        for(int i = 0; i < this.firstGate().dim; i++) {
            if(this.firstGate().Delta_clear_list.get(i) != null
                    && this.secondGate().Delta_clear_list.get(i) != null) {
                this.filter.add(true);
            }else{
                this.filter.add(false);
            }
        }
        // temp_x = $\Delta_x + \delta_x$
        List<BigInteger> temp_x = LinearAlgebra.addBigIntVec(this.firstGate().Delta_clear_list, delta_x_clear_list);
        // temp_y = $\Delta_y + \delta_y$
        List<BigInteger> temp_y = LinearAlgebra.addBigIntVec(this.secondGate().Delta_clear_list, delta_y_clear_list);
        //perform the filtering
        temp_x = LinearAlgebra.doFilter(temp_x, filter);
        temp_y = LinearAlgebra.doFilter(temp_y, filter);
        // temp_xy = temp_x * temp_y, 只有一个元素
        List<BigInteger> temp_xy = LinearAlgebra.dotProduct(temp_x, temp_y);
        // Compute [Delta_z] according to the paper
        // [Delta_z] = [c] + [lambda_z]
//        List<Share> Delta_z_shr = LinearAlgebra.addSharesVec(c_shr, lambda_share_list);
        c_shr = LinearAlgebra.doFilter(c_shr, this.filter); //mul the constant

        Share sumOf_c_share = LinearAlgebra.reduceSum(c_shr);
        List<Share> Delta_z_shr = List.of(sumOf_c_share); //update for multiplying a constant

        // [Delta_z] -= [a] * temp_y
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.dotProduct2(a_shr, temp_y));
        // [Delta_z] -= temp_x * [b]
        Delta_z_shr = LinearAlgebra.subtractSharesVec(Delta_z_shr, LinearAlgebra.dotProduct2(b_shr, temp_x));

        Delta_z_shr = LinearAlgebra.addSharesVec(Delta_z_shr, lambda_share_list);

        //To open Delta_z in the clear
        edgeServer.sendToKing(Delta_z_shr);
        if(edgeServer.isKing()){
            List<Object> shares = edgeServer.kingReadFromAll();
            List<BigInteger> values = Tool.openShares2Values(1, shares);
            values = LinearAlgebra.addBigIntVec(values, temp_xy); // temp_xy is not shared with other servers
            //Let other servers know Delta_clear_list, i.e., values
            edgeServer.kingSendToAll(values);
        }
        this.Delta_clear_list = (List<BigInteger>)edgeServer.readFromKing();
    }
}






