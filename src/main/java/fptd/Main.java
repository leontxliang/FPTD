package fptd;

import fptd.protocols.Circuit;
import fptd.truthDiscovery.optimized.TDOfflineOptimal;
import fptd.truthDiscovery.optimized.TDOnlineOptimal;
import fptd.utils.DataManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        if(Params.IS_PRINT_EXE_INFO) {
            System.out.println("Starting optimal TD...");
        }
        final String sensingDataFile = Params.sensingDataFile;
        final String truthFile = Params.truthFile;
        boolean isCategoricalData = Params.isCategoricalData;

        int requiredWorkerNum = -1;
        DataManager dataManager = new DataManager(sensingDataFile, truthFile, isCategoricalData, requiredWorkerNum);
        final int workerNum = dataManager.sensingDataMatrix.size();
        final int examNum = dataManager.sensingDataMatrix.get(0).size();

        if(Params.IS_PRINT_EXE_INFO){
            System.out.println("Finish to read sensing data from hard disk");
            System.out.println("Start to the offline phase");
        }

        final String jobName = "TD_optimal";

        runTDOffline(workerNum, examNum, jobName);

        if(Params.IS_PRINT_EXE_INFO){
            System.out.println("Finish to offline phase");
        }

        runTDOnline(workerNum, examNum, jobName, dataManager);

        if(Params.IS_PRINT_EXE_INFO){
            System.out.println("Finish to online phase");
        }
    }

    private static void runTDOffline(int workerNum, int examNum, String jobName) {
        new TDOfflineOptimal(workerNum, examNum, jobName).runTDOffline();
    }

    private static void runTDOnline(int workerNum,
                                    int examNum,
                                    String jobName,
                                    DataManager dataManager)
            throws InterruptedException {

        List<List<BigInteger>> worker2Labels = dataManager.sensingDataMatrix;
        if(worker2Labels.size() != workerNum) {
            throw new RuntimeException("Wrong number of workers");
        }
        if(worker2Labels.getFirst().size() != examNum) {
            throw new RuntimeException("Wrong number of exams");
        }

        if(Params.IS_PRINT_EXE_INFO){
            System.out.println("Start to build TD circuits");
        }

        TDOnlineOptimal tdOnline = new TDOnlineOptimal(workerNum, examNum);
        List<Circuit> circuits = tdOnline.buildTDCircuit(worker2Labels, jobName);

        if(Params.IS_PRINT_EXE_INFO){
            System.out.println("Finish to build TD circuits");
        }

        List<Thread> threads = new ArrayList<>();
        for(int owner_idx = 0; owner_idx < Params.NUM_SERVER; owner_idx++){
            Circuit circuit = circuits.get(owner_idx);
            Thread thread = new Thread(new ServerThread(circuit, owner_idx, owner_idx == 0, dataManager));
            thread.start();
            threads.add(thread);
            if (0 == owner_idx){//if it is the king
                Thread.sleep(500);
            }
        }
        for(Thread thread : threads){
            thread.join();
        }
    }
}