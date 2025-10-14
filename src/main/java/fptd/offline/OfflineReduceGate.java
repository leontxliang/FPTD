package fptd.offline;

import fptd.Params;
import fptd.Share;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OfflineReduceGate extends OfflineGate {

    public OfflineReduceGate(OfflineGate inputX) {
        super(inputX, null);
        this.dim = 1;
    }

    @Override
    void doRunOffline() {
        List<List<Share>> lambda_shares_matrix_temp = new ArrayList<List<Share>>();
        for(int i = 0; i < Params.N; i++){
            lambda_shares_matrix_temp.add(new ArrayList<>());
        }
        //for first column
        for(int i = 0; i < Params.N; i++){
            lambda_shares_matrix_temp.get(i).add(firstGate().lambda_shr_matrix.get(i).getFirst());
        }
        // sum the first column with other columns
        for(int j = 1; j < firstGate().dim; j++){
            for(int i = 0; i < Params.N; i++){
                Share shareInOtherColum = firstGate().lambda_shr_matrix.get(i).get(j);
                Share share_sum = lambda_shares_matrix_temp.get(i).getFirst().add(shareInOtherColum);
                lambda_shares_matrix_temp.get(i).set(0, share_sum);
            }
        }

        BigInteger lambda_clear = BigInteger.ZERO;
        for(int i = 0; i < firstGate().dim; i++){
            lambda_clear = lambda_clear.add(firstGate().lambda_clear_list.get(i));
        }
        this.lambda_clear_list = List.of(lambda_clear);
        this.lambda_shr_matrix = lambda_shares_matrix_temp;
    }
}
