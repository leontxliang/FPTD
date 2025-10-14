package fptd.offline;

import fptd.Share;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static fptd.Params.P;

public class OfflineElemWiseMultGate extends OfflineGate {


    public OfflineElemWiseMultGate(OfflineGate inputX, OfflineGate inputY) {
        super(inputX, inputY);
        if(inputX.dim != inputY.dim) {
            throw new IllegalArgumentException("Dims don't match");
        }
        this.dim = inputX.dim;
    }

    @Override
    void doRunOffline() {
        int size = this.dim;

        for (int i = 0; i < size; i++) {
            this.lambda_clear_list.add(Tool.getRand(64));
        }
        this.lambda_shr_matrix = fakeParty.generateAllPartiesShares(this.lambda_clear_list);

        List<BigInteger> a_clear_list = new ArrayList<>(size);
        List<BigInteger> b_clear_list = new ArrayList<>(size);
        List<BigInteger> c_clear_list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            BigInteger a = Tool.getRand(64);
            BigInteger b = Tool.getRand(64);
            a_clear_list.add(a);
            b_clear_list.add(b);
            c_clear_list.add(a.multiply(b).mod(P));
        }
        List<List<Share>> a_shares = fakeParty.generateAllPartiesShares(a_clear_list);
        List<List<Share>> b_shares = fakeParty.generateAllPartiesShares(b_clear_list);
        List<List<Share>> c_shares = fakeParty.generateAllPartiesShares(c_clear_list);

        // $\delta_x = a - \lambda_x$, $\delta_y = b - \lambda_y$
        List<BigInteger> delta_x_clear = LinearAlgebra.subtractBigIntVec(a_clear_list, this.firstGate().lambda_clear_list);
        List<BigInteger> delta_y_clear = LinearAlgebra.subtractBigIntVec(b_clear_list, this.secondGate().lambda_clear_list);

        //Write all data to files
        fakeParty.writeSharesToAllParties(a_shares);
        fakeParty.writeSharesToAllParties(b_shares);
        fakeParty.writeSharesToAllParties(c_shares);
        fakeParty.writeSharesToAllParties(lambda_shr_matrix);

        fakeParty.writeClearToAllParties(delta_x_clear);
        fakeParty.writeClearToAllParties(delta_y_clear);
    }
}
