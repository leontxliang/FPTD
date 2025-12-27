package fptd.truthDiscovery.optimized;

import static fptd.Params.IS_PRINT_EXE_INFO;
import static fptd.Params.ITER_TD;
import static fptd.Params.N;
import static fptd.Params.NUM_SERVER;
import static fptd.Params.PRECISE_ROUND;

import fptd.EdgeServer;
import fptd.Params;
import fptd.Share;
import fptd.protocols.Circuit;
import fptd.protocols.Gate;
import fptd.protocols.InputGate;
import fptd.protocols.OutputGate;
import fptd.protocols.ReduceGate;
import fptd.sharing.ShamirSharing;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TDOnlineOptimal {

    private int workerNum;
    private int examNum;

    public TDOnlineOptimal(int workerNum, int examNumPerWorker) {
        this.workerNum = workerNum;
        this.examNum = examNumPerWorker;
    }

    private List<List<Share>> initTruth() {
        List<List<Share>> result = new ArrayList<>();//shares of truth for each server
        for (int i = 0; i < Params.NUM_SERVER; i++) {
            result.add(new ArrayList<>());
        }
        Random rand = new Random();
        rand.setSeed(1);
        ShamirSharing sharing = new ShamirSharing();
        for (int i = 0; i < examNum; i++) {
            BigInteger truth = new BigInteger(2, rand);
            truth = truth.abs();
            truth = truth.multiply(BigInteger.valueOf(PRECISE_ROUND));
//            System.out.println("init truth: " + truth);
            List<Share> shares = sharing.getShares(truth);
            for (int serverIdx = 0; serverIdx < shares.size(); serverIdx++) {
                result.get(serverIdx).add(shares.get(serverIdx));
            }
        }
        return result;
    }

    /**
     * 通过每一个工人的标签数据计算出需要发给每一个server的秘密份额
     */
    public void getSharesForEachServer(List<List<BigInteger>> worker2labels,
            List<List<List<Share>>> server2w2e2shares_out,
            List<List<List<Share>>> server2e2w2shares_out) {
        if (worker2labels.size() != workerNum) {
            throw new IllegalArgumentException("worker2labels.size() != workerNum");
        }
        if (server2w2e2shares_out == null || server2e2w2shares_out == null) {
            throw new IllegalArgumentException("w2e2shares_out == null || w2e2shares_out == null");
        }
        server2w2e2shares_out.clear();
        server2e2w2shares_out.clear();
        for (int i = 0; i < Params.NUM_SERVER; i++) {
            List<List<Share>> a_server1 = new ArrayList<>();
            List<List<Share>> a_server2 = new ArrayList<>();
            for (int j = 0; j < workerNum; j++) {
                List<Share> workers = new ArrayList<>();
                a_server1.add(workers);
            }
            for (int j = 0; j < examNum; j++) {
                List<Share> exams = new ArrayList<>();
                a_server2.add(exams);
            }
            server2w2e2shares_out.add(a_server1);//每一行表示一个服务器所收到的感知数据的share对象
            server2e2w2shares_out.add(a_server2);//每一行表示一个服务器所收到的感知数据的share对象
        }
        ShamirSharing sharing = new ShamirSharing();
        for (int workerIdx = 0; workerIdx < workerNum; workerIdx++) {//每一个用户
            for (int examIdx = 0; examIdx < examNum; examIdx++) {//每一个exam
                BigInteger label = worker2labels.get(workerIdx).get(examIdx);
                List<Share> shares = null;
                if (label != null) {
                    shares = sharing.getShares(label);
                } else {
                    shares = new ArrayList<>(Collections.nCopies(N, null));
                }
                //把该label数据的份额分别发给不同的服务器
                for (int serverIdx = 0; serverIdx < shares.size(); serverIdx++) {
                    server2w2e2shares_out.get(serverIdx).get(workerIdx).add(shares.get(serverIdx));
                    server2e2w2shares_out.get(serverIdx).get(examIdx).add(shares.get(serverIdx));
                }
            }
        }
    }

    public List<Circuit> buildTDCircuit(List<List<BigInteger>> worker2labels, String jobName) {
        //Workers prepare secret shares
        List<List<List<Share>>> server2w2e2shares = new ArrayList<>();
        List<List<List<Share>>> server2e2w2shares = new ArrayList<>();
        getSharesForEachServer(worker2labels, server2w2e2shares, server2e2w2shares);
        List<List<Share>> truthSharesForEachServer = initTruth();

        List<EdgeServer> servers = new ArrayList<EdgeServer>();
        for (int i = 0; i < Params.NUM_SERVER; i++) {
            if (i == 0) {
                EdgeServer king = new EdgeServer(true, i, jobName);
                servers.add(king);
            } else {
                servers.add(new EdgeServer(false, i, jobName));
            }
        }
        return doBuildTDCircuit(servers, server2w2e2shares, server2e2w2shares, truthSharesForEachServer);
    }

    public List<Circuit> doBuildTDCircuit(List<EdgeServer> servers,
            final List<List<List<Share>>> server2w2e2shares,
            final List<List<List<Share>>> server2e2w2shares,
            final List<List<Share>> truthSharesForEachServer) {
        List<Circuit> circuits = new ArrayList<>();
        for (int i = 0; i < NUM_SERVER; i++) {
            Circuit circuit = new Circuit(servers.get(i));
            circuits.add(circuit);
        }

        //build the circuit for each edge server
        for (int server_idx = 0; server_idx < NUM_SERVER; server_idx++) {
            Circuit circuit = circuits.get(server_idx);
            int dim = this.examNum;
            InputGate in_truth = circuit.input(server_idx, dim); // 针对这个circuit的初始化的真值
            List<InputGate> sensingDataPerWorker = new ArrayList<>(); //每一个工人针对这个circuit的input gate
            for (int workerIdx = 0; workerIdx < workerNum; workerIdx++) {
                InputGate in_sensing_data = circuit.input(server_idx, dim);//设置每一个worker的感知数据为一个input
                in_sensing_data.setInput(
                        server2w2e2shares.get(server_idx).get(workerIdx));//set the input for all workers' sensing data
                in_truth.setInput(truthSharesForEachServer.get(server_idx)); //set the input for the estimated truths
                sensingDataPerWorker.add(in_sensing_data);
            }

            Gate estimatedTruthGate = in_truth;

            //为计算真值准备：设置每一个exam所对应的所有sensing data。只需要准备一次
            List<Gate> sensingDataPerExam = new ArrayList<>();
            for (int examIdx = 0; examIdx < examNum; examIdx++) {
                int dimension = workerNum;
                InputGate in_sensing_data_per_exam = circuit.input(server_idx, dimension);
                in_sensing_data_per_exam.setInput(server2e2w2shares.get(server_idx).get(examIdx));
                sensingDataPerExam.add(in_sensing_data_per_exam);
            }

            for (int iter = 0; iter < ITER_TD; iter++) {
                List<Gate> subGates = new ArrayList<>();
                for (int workerIdx = 0; workerIdx < workerNum; workerIdx++) {
                    //Calc distance between the sensing data and the estimated truths
                    Gate subGate = circuit.subtract(sensingDataPerWorker.get(workerIdx), estimatedTruthGate);
                    subGates.add(subGate);
                }
                Gate allSubGate = circuit.combination(subGates.toArray(new Gate[subGates.size()]));
                Gate sumUpAllDistance = circuit.dotProdWithFilter(allSubGate, allSubGate);

                //一起处理所有工人的距离
                OutputGate outSumUpAllDistance = circuit.output(sumUpAllDistance);
                if (IS_PRINT_EXE_INFO) {
                    outSumUpAllDistance.setName("sumUpAllDistance-iter" + (iter + 1));
                }
                Gate divGate = circuit.dotProdThenDivGate(subGates, subGates, outSumUpAllDistance,
                        BigInteger.valueOf(100000));//避免分子小于分母
                //含所有工人的权重,即维度等于工人的个数
                Gate weightGate = circuit.logarithm(divGate);

                if (IS_PRINT_EXE_INFO) {
                    OutputGate in_sensing_dataGate = circuit.output(sensingDataPerWorker.get(0));
                    in_sensing_dataGate.setName("worker sensing data" + 0 + ":");
                    circuit.addEndpoint(in_sensing_dataGate);

                    OutputGate estimatedTruthOutputGate = circuit.output(estimatedTruthGate);
                    estimatedTruthOutputGate.setName("truth" + 0);
                    circuit.addEndpoint(estimatedTruthOutputGate);

                    OutputGate divGateTemp = circuit.output(divGate);
                    divGateTemp.setName("divGate" + (iter + 1));
                    circuit.addEndpoint(divGateTemp);

                    OutputGate weightsTemp = circuit.output(weightGate);
                    weightsTemp.setName("weights" + (iter + 1));
                    circuit.addEndpoint(weightsTemp);
                }

                //-------------Calculate truth---------------
                //某些worker可能不提供数据给某exam
                ReduceGate[] sumWeightGates = new ReduceGate[examNum];
                for (int examIdx = 0; examIdx < examNum; examIdx++) {
                    List<Share> filter = server2e2w2shares.get(server_idx).get(examIdx);
                    //并非相加所有权重，只求和提交了数据给该exam的workers的权重
                    ReduceGate sumOfWeightsGate = circuit.reduceSum(weightGate, filter);
                    sumWeightGates[examIdx] = sumOfWeightsGate;
                }
                //不同维度sumWeight的值不同
                OutputGate outputSumOfWeightsGate = circuit.output(circuit.combination(sumWeightGates));
                if (IS_PRINT_EXE_INFO) {
                    outputSumOfWeightsGate.setName("SumOfWeightsPerExam" + (iter + 1));
                }
                List<Gate> weightGates = new ArrayList<>();
                for (int examIdx = 0; examIdx < examNum; examIdx++) {
                    weightGates.add(weightGate);
                }
                estimatedTruthGate = circuit.dotProdThenDivGate(sensingDataPerExam, weightGates,
                        outputSumOfWeightsGate);
                if (IS_PRINT_EXE_INFO) {
                    //目的是打印每一个iter下的真值
                    OutputGate estimatedTruthOutputGate = circuit.output(estimatedTruthGate);
                    estimatedTruthOutputGate.setName("truth" + (iter + 1));
                    circuit.addEndpoint(estimatedTruthOutputGate);

                    OutputGate in_sensing_data_per_exam_gatesTemp = circuit.output(sensingDataPerExam.get(0));
                    in_sensing_data_per_exam_gatesTemp.setName("sensingDataPerExam");
                    circuit.addEndpoint(in_sensing_data_per_exam_gatesTemp);
                }

            }//End iteration of TD

            if (!IS_PRINT_EXE_INFO) {
                OutputGate truthOutputGate = circuit.output(estimatedTruthGate);
                truthOutputGate.setName("truth-final");
                circuit.addEndpoint(truthOutputGate);
            }
        }
        return circuits;
    }

}
