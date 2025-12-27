package fptd.protocols;

import fptd.EdgeServer;
import fptd.Share;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Circuit {

    private EdgeServer server;
    private List<Gate> gates = new ArrayList<>();
    private List<Gate> endpoints = new ArrayList<>();
    private List<OutputGate> outputGates = new ArrayList<>();

    public Circuit(EdgeServer server) {
        this.server = server;
    }

    public void addEndpoint(Gate gate) {
        endpoints.add(gate);
    }

    public void runOffline() {
        for (Gate gate : endpoints) {
            gate.runOffline();
        }
    }

    public void readOfflineFromFile() {
        for (Gate gate : endpoints) {
            gate.readOfflineFromFile();
        }
    }

    public void runOnline() {
        for (Gate gate : endpoints) {
            gate.runOnline();
        }
    }

    public void runOnlineWithBenckmark() {
        runOnline();
    }

    public void printStats() {
    }

    public InputGate input(int owner_id, int dim) {
        InputGate gate = new InputGate(server, dim, owner_id);
        this.gates.add(gate);
        return gate;
    }

    public ScalingGate scaling(Gate inputX, BigInteger scalingFactor) {
        ScalingGate gate = new ScalingGate(inputX, scalingFactor);
        this.gates.add(gate);
        return gate;
    }

    public LogarithmGate logarithm(Gate inputX) {
        LogarithmGate gate = new LogarithmGate(inputX);
        this.gates.add(gate);
        return gate;
    }

    public ReduceGate reduceSum(Gate inputX) {
        ReduceGate gate = new ReduceGate(inputX);
        this.gates.add(gate);
        return gate;
    }

    // filter中部分元素可能为null，将InputX所对应的这些元素也设置为null
    public <T> ReduceGate reduceSum(Gate inputX, List<Share> filter) {
        ReduceGate gate = new ReduceGate(inputX, filter);
        this.gates.add(gate);
        return gate;
    }

    public AddGate add(Gate inputX, Gate inputY) {
        AddGate gate = new AddGate(inputX, inputY);
        gates.add(gate);
        return gate;
    }

    public SubtractGate subtract(Gate inputX, Gate inputY) {
        SubtractGate gate = new SubtractGate(inputX, inputY);
        gates.add(gate);
        return gate;
    }


    public SubtractGate subtract(Gate inputX, Gate inputY, List<List<Boolean>> missingValueMatrix) {
        SubtractGate gate = new SubtractGate(inputX, inputY, missingValueMatrix);
        gates.add(gate);
        return gate;
    }

    public ElemMulThenDivGate elemMulThenDivGate(Gate inputX, Gate inputY, OutputGate divisorGate) {
        ElemMulThenDivGate gate = new ElemMulThenDivGate(inputX, inputY, divisorGate);
        this.gates.add(gate);
        return gate;
    }

    public ElemMulThenDivGate elemMulThenDivGate(Gate inputX, Gate inputY, List<BigInteger> divisors) {
        ElemMulThenDivGate gate = new ElemMulThenDivGate(inputX, inputY, divisors);
        this.gates.add(gate);
        return gate;
    }


    public DotProdThenDivGate dotProdThenDivGate(List<Gate> inputXGates,
            List<Gate> inputYGates,
            OutputGate divisorGate) {
        List<Gate> input = new ArrayList<>();
        input.addAll(inputXGates);
        input.addAll(inputYGates);
        input.add(divisorGate);
        DotProdThenDivGate gate = new DotProdThenDivGate(input);
        this.gates.add(gate);
        return gate;
    }

    public DotProdThenDivGate dotProdThenDivGate(List<Gate> inputXGates,
            List<Gate> inputYGates,
            OutputGate divisorGate,
            BigInteger scalingFactor) {
        List<Gate> input = new ArrayList<>();
        input.addAll(inputXGates);
        input.addAll(inputYGates);
        input.add(divisorGate);
        DotProdThenDivGate gate = new DotProdThenDivGate(input, scalingFactor);
        this.gates.add(gate);
        return gate;
    }


    public CombinationGate combination(Gate... inputGates) {
        CombinationGate gate = new CombinationGate(inputGates);
        this.gates.add(gate);
        return gate;
    }

    public AddConstantGate addConstant(Gate inputX, List<BigInteger> constants) {
        AddConstantGate gate = new AddConstantGate(inputX, constants);
        gates.add(gate);
        return gate;
    }

    public DivisionGate div(Gate inputX, List<BigInteger> divisors) {
        DivisionGate gate = new DivisionGate(inputX, divisors);
        gates.add(gate);
        return gate;
    }

    public DivisionGate div(Gate inputX, OutputGate inputY) {
        DivisionGate gate = new DivisionGate(inputX, inputY);
        gates.add(gate);
        return gate;
    }

    public DotProductGate dotProduct(Gate inputX, Gate inputY) {
        DotProductGate gate = new DotProductGate(inputX, inputY);
        gates.add(gate);
        return gate;
    }

    public DotProdWithFilterGate dotProdWithFilter(Gate inputX, Gate inputY) {
        DotProdWithFilterGate gate = new DotProdWithFilterGate(inputX, inputY);
        gates.add(gate);
        return gate;
    }

    public ElemWiseMultiplyGate elemMultiply(Gate inputX, Gate inputY) {
        ElemWiseMultiplyGate gate = new ElemWiseMultiplyGate(inputX, inputY);
        gates.add(gate);
        return gate;
    }

    public ElemWiseMulThenMulConstGate elemWiseMulThenMulConst(Gate inputX,
            Gate inputY, List<BigInteger> constants) {
        ElemWiseMulThenMulConstGate gate = new ElemWiseMulThenMulConstGate(inputX, inputY, constants);
        gates.add(gate);
        return gate;
    }

    public OutputGate output(Gate inputX) {
        OutputGate gate = new OutputGate(inputX);
        gates.add(gate);
        outputGates.add(gate);
        return gate;
    }

    public EdgeServer getServer() {
        return server;
    }

    public List<List<BigInteger>> getOutputValues() {
        List<List<BigInteger>> outputValues = new ArrayList<>();
        this.outputGates.forEach(x -> outputValues.add(x.getOutputValues()));
        return outputValues;
    }

    public List<List<BigInteger>> getOutputValues(List<String> names_out) {
        List<List<BigInteger>> outputValues = new ArrayList<>();
        names_out.clear();
        this.outputGates.forEach(x -> outputValues.add(x.getOutputValues()));
        this.outputGates.forEach(x -> names_out.add(x.getName()));
        return outputValues;
    }

    public List<Gate> getEndpoints() {
        return endpoints;
    }

    public void clearEndpoints() {
        endpoints.clear();
    }

    public void clearOutputGates() {
        outputGates.clear();
    }
}


