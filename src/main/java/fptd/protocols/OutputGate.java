package fptd.protocols;

import fptd.utils.LinearAlgebra;
import fptd.utils.Tool;
import java.math.BigInteger;
import java.util.List;

public class OutputGate extends Gate {

    private List<BigInteger> outputValues;
    private String name = "";

    public OutputGate(final Gate inputX) {
        super(inputX, null);
        this.dim = inputX.dim;
    }

    @Override
    void doReadOfflineFromFile() {
    }

    @Override
    void doRunOnline() {
        this.lambda_share_list = this.firstGate().lambda_share_list;
        this.Delta_clear_list = this.firstGate().Delta_clear_list;

        edgeServer.sendToKing(this.lambda_share_list);
        if (edgeServer.isKing()) {
            //二维数组，每一行表示一个server，每一列表示一个secret的share
            List<Object> receivedShares = edgeServer.kingReadFromAll(); // receive from the network
            List<BigInteger> lambda_clear_list_temp = Tool.openShares2Values(dim, receivedShares);

            //test
//            lambda_clear_list_temp.forEach((x)->{
//                System.out.println("lambda clear in outputgate = " + x);
//            });

            List<BigInteger> outputValues = LinearAlgebra.subtractBigIntVec(Delta_clear_list, lambda_clear_list_temp);
//            this.outputValues = outputValues;

            edgeServer.kingSendToAll(outputValues);
        }
        this.outputValues = (List<BigInteger>) edgeServer.readFromKing();
    }

    public List<BigInteger> getOutputValues() {
        return outputValues;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
