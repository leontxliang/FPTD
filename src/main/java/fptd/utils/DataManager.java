package fptd.utils;

import fptd.Params;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class DataManager {
//    private final static String datasetName = "weather";
//    private final String datasetName = "d_Duck_Identification";
    public static Map<String, Double> groundTruths ;
    public static List<String> arrayIdx2ExamID = new ArrayList<>(); // an index
    public static List<String> arrayIdx2WorkerID = new ArrayList<>();
    public static List<List<BigInteger>> sensingDataMatrix = null;
    public static int requiredWorkerNum = -1;

    public static boolean isCategoricalData = true;

    /**
     *
     * @param sensingDataFile
     * @param truthFile
     * @param isCategoricalData
     * @param requiredWorkerNum 指定的只处理前特定数量的工人的数据
     */
    public DataManager(String sensingDataFile, String truthFile, boolean isCategoricalData, int requiredWorkerNum) {
        //Worker to labels
        Map<String, Map<String, BigInteger>> sensingData;
        sensingData = DataManager.readSensingData(sensingDataFile, Params.PRECISE_ROUND);
        Map<String, Map<String, BigInteger>> chosenSensingData = null;
        if(requiredWorkerNum > 0){//只保留前requiredWorkerNum个工人的数据
            chosenSensingData = new TreeMap<>();
            Object [] workerIDs = sensingData.keySet().toArray();
            if(Params.sensingDataFile.contains("Dog") || Params.sensingDataFile.contains("weather")){
                //如果是dog数据集
                workerIDs = sensingData.keySet().stream().map(Integer::parseInt).sorted().toArray();
            }else if (Params.sensingDataFile.contains("Duck")){
                //如果是duck数据集
                workerIDs = new Object[]{896, 866, 39, 175, 1721, 1722, 1723, 1724, 1725, 1726, 1727, 1730, 1731, 1733, 1734, 97, 1737, 1738, 1740, 1741, 1742, 335, 1750, 1755, 1756, 1757, 1758, 1759, 1760, 1761, 1762, 1763, 1764, 1765, 1766, 1005, 885, 1743, 1023};
            }

//            Set<String> set = new HashSet<String>();
//            set.addAll(sensingData.keySet());
//            Object [] workerIDs = set.toArray();

            for(int i = 0; i < requiredWorkerNum; i++){
                String chosenWorkerID = workerIDs[i].toString();
                chosenSensingData.put(chosenWorkerID, sensingData.get(chosenWorkerID));
            }
        }else{
            chosenSensingData = sensingData;
        }
        groundTruths = DataManager.readTruthData(truthFile);
        sensingDataMatrix = DataManager.changeToMatrix(chosenSensingData, arrayIdx2ExamID, arrayIdx2WorkerID);
        this.isCategoricalData = isCategoricalData;
        this.requiredWorkerNum = requiredWorkerNum;
    }

    public DataManager(String sensingDataFile, String truthFile, boolean isCategoricalData) {
        this(sensingDataFile, truthFile, isCategoricalData, -1);
    }

    /**
     * @param path
     * @param preciseRound
     * @return worker to examID to label
     * @throws IOException
     */
    public static Map<String, Map<String, BigInteger>> readSensingData(String path, long preciseRound)  {
        //worker to exam to labels
        Map<String, Map<String, BigInteger>> result = new TreeMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split(",");
                String examID = words[0];
                String workerID = words[1];
                if("worker".equals(workerID) || line.isEmpty()) {
                    continue;
                }
                String label = words[2];
                if(!result.containsKey(workerID)){
                    result.put(workerID, new TreeMap<>());
                }
                long label2 = (long)(Double.valueOf(label) * preciseRound);
                result.get(workerID).put(examID, BigInteger.valueOf(label2));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static Map<String, Double> readTruthData(String path) {
        Map<String, Double> result = new TreeMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.split(",");
                String examID = words[0];
                String label = words[1];
                if("truth".equals(label)) {
                    continue;
                }
                result.put(examID, Double.valueOf(label));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }


    /**
     * 将工人的数据从map映射转换成二维的矩阵。Fill the missing value with null
     * @param w2e2l_map
     * @return worker to labels matrix
     */
    public static List<List<BigInteger>> changeToMatrix(Map<String, Map<String, BigInteger>> w2e2l_map,
                            List<String> arrayIdx2ExamIDOut, List<String> arrayIdx2WorkerIDOut) {
        if(arrayIdx2ExamIDOut == null || arrayIdx2WorkerIDOut == null) {
            throw new IllegalArgumentException("arrayIdx2ExamIDOut or arrayIdx2WorkerIDOut cannot be null");
        }

        Set<String> examIDSet = new TreeSet<>();
        for(Map.Entry<String, Map<String, BigInteger>> entry : w2e2l_map.entrySet()){
            examIDSet.addAll(entry.getValue().keySet());
        }
        arrayIdx2ExamIDOut.clear();
        arrayIdx2ExamIDOut.addAll(examIDSet);
        arrayIdx2WorkerIDOut.addAll(w2e2l_map.keySet().stream().toList());

        List<List<BigInteger>> result = new ArrayList<>();
        for(int workerIdx = 0; workerIdx < arrayIdx2WorkerIDOut.size(); workerIdx++){
            List<BigInteger> labelsThisWorker = new ArrayList<>();//对每一个行（即每个worker）初始化空的数组
            result.add(labelsThisWorker);

            String workerID = arrayIdx2WorkerIDOut.get(workerIdx);
            Map<String, BigInteger> e2labelsOfThisWorker = w2e2l_map.get(workerID);
            for(int examIdx = 0; examIdx < arrayIdx2ExamIDOut.size(); examIdx++){
                String examID = arrayIdx2ExamIDOut.get(examIdx);
                if(e2labelsOfThisWorker.containsKey(examID)){
                    labelsThisWorker.add(e2labelsOfThisWorker.get(examID));
                }else{//not exist
                    labelsThisWorker.add(null);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
//        final String sensingDataFile = "datasets/d_Duck_Identification/answer.csv";
//        final String truthFile = "datasets/d_Duck_Identification/truth.csv";

        //39个worker，108个exam，
//        String sensingDataFile = "datasets/d_Duck_Identification/answer.csv";
//        String truthFile = "datasets/d_Duck_Identification/truth.csv";

        //worker num = 176, exam num = 8315
//        String sensingDataFile = "datasets/d_jn-product/answer.csv";
//        String truthFile = "datasets/d_jn-product/truth.csv";

        //109, 807
//        String sensingDataFile = "datasets/s4_Dog_data/answer.csv";
//        String truthFile = "datasets/s4_Dog_data/truth.csv";

        //27, 584
//        String sensingDataFile = "datasets/s4_Face_Sentiment_Identification/answer.csv";
//        String truthFile = "datasets/s4_Face_Sentiment_Identification/truth.csv";

        String sensingDataFile = "datasets/weather/answer.csv";
        String truthFile = "datasets/weather/truth.csv";


        //9, 1400
        DataManager dataManager = new DataManager(sensingDataFile, truthFile, false);
        System.out.println(dataManager);

    }
}



















