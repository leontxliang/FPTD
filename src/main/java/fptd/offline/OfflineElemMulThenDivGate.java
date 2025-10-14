package fptd.offline;

import fptd.Share;
import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static fptd.Params.P;

public class OfflineElemMulThenDivGate extends OfflineGate {

    public OfflineElemMulThenDivGate(OfflineGate inputX, OfflineGate inputY,
                                     OfflineOutputGate divisorGate){
        super(inputX, inputY, divisorGate);
        if(inputX.dim != inputY.dim || inputX.dim != divisorGate.dim){
            throw new RuntimeException("Dims don't match");
        }
    }

    @Override
    void doRunOffline() {
        {   // For multiplication
            int size = this.dim;

//            for (int i = 0; i < size; i++) {
//                this.lambda_clear_list.add(Tool.getRand(64));
//            }
//            this.lambda_shr_matrix = fakeParty.generateAllPartiesShares(this.lambda_clear_list);

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
//            fakeParty.writeSharesToAllParties(lambda_shr_matrix);

            fakeParty.writeClearToAllParties(delta_x_clear);
            fakeParty.writeClearToAllParties(delta_y_clear);
        }

        {   //For division
            Random rand = new Random();
            List<BigInteger> r_list = new ArrayList<>();
            List<BigInteger> r1_list = new ArrayList<>();
            List<BigInteger> r2_list = new ArrayList<>();
            for(int i = 0; i < dim; i++) {
                BigInteger r = new BigInteger(OfflineDivisionGate.l + OfflineDivisionGate.sigma, rand);
                BigInteger r1 = new BigInteger(OfflineDivisionGate.e + OfflineDivisionGate.sigma, rand);
                BigInteger r2 = new BigInteger(OfflineDivisionGate.l + OfflineDivisionGate.sigma, rand);
                r_list.add(r);
                r1_list.add(r1);
                r2_list.add(r2);
            }
            List<List<Share>> shares_r = this.fakeParty.generateAllPartiesShares(r_list);
            List<List<Share>> shares_r1 = this.fakeParty.generateAllPartiesShares(r1_list);
            List<List<Share>> shares_r2 = this.fakeParty.generateAllPartiesShares(r2_list);
            this.fakeParty.writeSharesToAllParties(shares_r);
            this.fakeParty.writeSharesToAllParties(shares_r1);
            this.fakeParty.writeSharesToAllParties(shares_r2);

            this.lambda_clear_list = r1_list;
            this.lambda_shr_matrix = shares_r1;
        }


    }
}
