package fptd.protocols;

import static fptd.Params.T;

import fptd.EdgeServer;
import fptd.Share;
import fptd.sharing.ShamirSharing;
import fptd.utils.LinearAlgebra;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class InputGate extends Gate {
    private int owner_id;
    private List<Share> sharesOfSecrets;

    public InputGate(EdgeServer server, int dim, int owner_id) {
        super(server, dim);
        if (server.getIdx() != owner_id) {
            throw new IllegalArgumentException("Server id mismatch");
        }
        this.owner_id = owner_id;
    }

    public void setInput(List<Share> sharesOfSecrets) {
        if (sharesOfSecrets.size() != getDim()) {
            throw new IllegalArgumentException("Input size mismatch");
        }
        this.sharesOfSecrets = sharesOfSecrets;
    }

    @Override
    void doReadOfflineFromFile() {
        int size = this.dim;
        this.lambda_share_list = edgeServer.readRandShares(size);
    }

    @Override
    void doRunOnline() {
        List<Share> shares = LinearAlgebra.addSharesVec(sharesOfSecrets, lambda_share_list);

//        if(Params.IS_PRINT_COMM_SIZE) {
//            synchronized (allDataSizeSentByPartiesOnline) {
//                List<List<BigInteger>> temp = DataManager.filterMatrix;
////                System.out.println();
////                    allDataSizeSentByPartiesOnline += Tool.getObjSize(message);
//            }
//        }

        edgeServer.sendToKing(shares);

        if (this.edgeServer.isKing()) {
            ShamirSharing sharing = new ShamirSharing();
            List<BigInteger> Delta_clear_list_temp = new ArrayList<>();
            //二维数组，每一行表示一个server，每一列表示一个secret的share
            List<Object> receivedShares = edgeServer.kingReadFromAll(); // receive from the network
            for (int colIdx = 0; colIdx < getDim(); colIdx++) {
                List<Share> shrsToRecover = new ArrayList<>(T);
                for (int rowIdx = 0; rowIdx < T; rowIdx++) {
                    List<Share> row = (List<Share>) receivedShares.get(rowIdx);
                    shrsToRecover.add(row.get(colIdx));
                }
                if (!shrsToRecover.isEmpty() && shrsToRecover.get(0) != null) {
                    BigInteger Delta_clear = sharing.recover(shrsToRecover);
                    Delta_clear_list_temp.add(Delta_clear);
                } else {
                    Delta_clear_list_temp.add(null);
                }
            }
            edgeServer.kingSendToAll(Delta_clear_list_temp); // The king sends via the network
        }
        this.Delta_clear_list = (List<BigInteger>) edgeServer.readFromKing(); // Receive from the king
    }
}













