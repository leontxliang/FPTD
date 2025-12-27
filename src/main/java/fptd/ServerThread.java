package fptd;

import static fptd.Params.IS_PRINT_EXE_INFO;

import fptd.protocols.Circuit;
import fptd.utils.DataManager;
import fptd.utils.Metric;
import fptd.utils.Tool;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ServerThread implements Runnable {

    private Circuit circuit;
    private boolean isKing = false;
    private EdgeServer server;
    private DataManager dataManager;

    public ServerThread(Circuit circuit, int idx, boolean isKing) {
        this.circuit = circuit;
        this.isKing = isKing;
        this.server = circuit.getServer();
    }

    public ServerThread(Circuit circuit, int idx, boolean isKing, DataManager dataManager) {
        this(circuit, idx, isKing);
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        try {
            server.connectOtherServers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (Params.IS_PRINT_EXE_INFO) {
            System.out.println("start to read offline randomness from file.");
        }

        circuit.readOfflineFromFile();

        if (Params.IS_PRINT_EXE_INFO) {
            System.out.println("Server" + server.getIdx() + " start to run online circuit.");
        }

        circuit.runOnline();

        if (Params.IS_PRINT_EXE_INFO) {
            System.out.println("Server" + server.getIdx() + " end to run online circuit.");
        }
        if (isKing) {
            List<String> namesOfOutputGates = new ArrayList<>();
            List<List<BigInteger>> result = circuit.getOutputValues(namesOfOutputGates);

            for (int i = 0; i < result.size(); i++) {
                String name = namesOfOutputGates.get(i);
                if (name.isEmpty()) {
                    continue;
                }
                List<BigInteger> predictedTruths = result.get(i);

                if (IS_PRINT_EXE_INFO) {
                    System.out.print(name + ": ");
                    for (BigInteger value : predictedTruths) {
                        System.out.print(value + "\t");
                    }
                    System.out.println();
                }

                if (name.startsWith("truth")) {
                    Map<String, BigInteger> predictedTruthsMap = new TreeMap<>();
                    for (int examIdx = 0; examIdx < DataManager.arrayIdx2ExamID.size(); examIdx++) {
                        String examID = DataManager.arrayIdx2ExamID.get(examIdx);
                        if (DataManager.groundTruths.containsKey(examID)) {
                            predictedTruthsMap.put(examID, predictedTruths.get(examIdx));
                        }
                    }
                    double acc = Tool.getAccuracy(predictedTruthsMap, DataManager.groundTruths,
                            Params.PRECISE_ROUND, Metric.RMSE, DataManager.isCategoricalData);
                    if (IS_PRINT_EXE_INFO) {
                        System.out.println("Accuracy of " + name + " = " + acc);
                    }

                } else if (name.startsWith("weights")) {
                    BigInteger sum = BigInteger.ZERO;
                    for (BigInteger value : predictedTruths) {
                        sum = sum.add(value);
                    }
                    System.out.println("Sum of " + name + " = " + sum);
                }


            }
        }
        this.server.close();
    }
}
