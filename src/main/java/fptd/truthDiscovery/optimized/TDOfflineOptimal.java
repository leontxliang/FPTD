package fptd.truthDiscovery.optimized;

import fptd.Params;
import fptd.offline.*;

import java.util.ArrayList;
import java.util.List;

import static fptd.Params.IS_PRINT_EXE_INFO;
import static fptd.Params.N;

public class TDOfflineOptimal {

    private int workerNum;
    private int examNum;
    private String jobName;

    public TDOfflineOptimal(int workerNum, int examNum, String jobName) {
        this.workerNum = workerNum;
        this.examNum = examNum;
        this.jobName = jobName;
    }

    public void runTDOffline() {
        FakeParty fakeParty = new FakeParty(jobName, N);
        OfflineCircuit circuit = new OfflineCircuit(fakeParty);

        int dim = this.examNum;
        OfflineInputGate in_truth = circuit.input(0, dim);;
        List<OfflineInputGate> in_sensing_data_list = new ArrayList<>();
        for(int workerIdx = 0; workerIdx < this.workerNum; workerIdx++) {
            OfflineInputGate in_sensing_data = circuit.input(0, dim);
            in_sensing_data_list.add(in_sensing_data);
        }

        OfflineGate estimatedTruthGate = in_truth;

        // The input for the phase of calculating truth values
        List<OfflineGate> in_sensing_data_per_exam_gates= new ArrayList<>();
        for(int examIdx = 0; examIdx < this.examNum; examIdx++) {
            int dimension = workerNum;
            OfflineInputGate in_sensing_data_per_exam = circuit.input(0, dimension);
            in_sensing_data_per_exam_gates.add(in_sensing_data_per_exam);
        }

        for(int iter = 0; iter < Params.ITER_TD; iter++) {
            List<OfflineGate> subGates = new ArrayList<>();
            for(int workerIdx = 0; workerIdx < this.workerNum; workerIdx++) {
                OfflineGate subGate = circuit.subtract(in_sensing_data_list.get(workerIdx), estimatedTruthGate);
                subGates.add(subGate);
            }
            OfflineGate allSubGate = circuit.combination(subGates.toArray(new OfflineGate[subGates.size()]));
            OfflineGate sumUpAllDistance = circuit.dotProdWithFilter(allSubGate, allSubGate);
            OfflineGate divGate = circuit.dotProdThenDivGate(subGates, subGates, circuit.output(sumUpAllDistance));
            OfflineGate weightGate = circuit.log(divGate);

            if(IS_PRINT_EXE_INFO){
                OfflineOutputGate in_sensing_dataGate = circuit.output(in_sensing_data_list.get(0));
                circuit.addEndpoint(in_sensing_dataGate);

                OfflineOutputGate estimatedTruthOutputGate = circuit.output(estimatedTruthGate);
                circuit.addEndpoint(estimatedTruthOutputGate);

                OfflineOutputGate divGateTemp = circuit.output(divGate);
                circuit.addEndpoint(divGateTemp);

                OfflineOutputGate weightGateTemp = circuit.output(weightGate);
                circuit.addEndpoint(weightGateTemp);
            }

            /*************To calculate truth values*****************/
            //某些worker可能不提供数据给某exam
            OfflineReduceGate[] sumWeightGates = new OfflineReduceGate[examNum];
            for(int examIdx = 0; examIdx < this.examNum; examIdx++) {
                OfflineReduceGate sumOfWeightsGate = circuit.reduce(weightGate);
//                FakeOutputGate outputWeightGate = circuit.output(sumOfWeightsGate);
                sumWeightGates[examIdx] = sumOfWeightsGate;
            }
            //不同维度sumWeight的值不同
            OfflineOutputGate outputSumOfWeightsGate = circuit.output(circuit.combination(sumWeightGates));

            List<OfflineGate> weightGates = new ArrayList<>();
            for(int examIdx = 0; examIdx < this.examNum; examIdx++) {
                weightGates.add(weightGate);
            }
            estimatedTruthGate = circuit.dotProdThenDivGate(in_sensing_data_per_exam_gates,
                    weightGates, outputSumOfWeightsGate);

            if(IS_PRINT_EXE_INFO) {
                //目的是打印每一个iter下的真值
                OfflineOutputGate estimatedTruthOutputGate = circuit.output(estimatedTruthGate);
                circuit.output(estimatedTruthOutputGate);
                circuit.addEndpoint(estimatedTruthOutputGate);

                OfflineOutputGate in_sensing_data_per_exam_gatesTemp = circuit.output(in_sensing_data_per_exam_gates.get(0));
                circuit.output(in_sensing_data_per_exam_gatesTemp);
                circuit.addEndpoint(in_sensing_data_per_exam_gatesTemp);
            }
        }
        if(!IS_PRINT_EXE_INFO) {
            OfflineOutputGate truthOutputGate = circuit.output(estimatedTruthGate);
            circuit.addEndpoint(truthOutputGate);//The second endpoint
        }
        circuit.runOffline();
    }

}
