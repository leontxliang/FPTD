package fptd.offline;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OfflineCircuit {

    private FakeParty fakeParty;
    private List<OfflineGate> gates = new ArrayList<>();
    private List<OfflineGate> endpoints = new ArrayList<>();

    public OfflineCircuit(FakeParty fakeParty) {
        this.fakeParty = fakeParty;
    }

    public void runOffline() {
        for (OfflineGate gate : endpoints) {
            gate.runOffline();
        }
    }

    public void addEndpoint(OfflineGate gate) {
        this.endpoints.add(gate);
    }

    public OfflineInputGate input(int owner_id, int dim) {
        OfflineInputGate gate = new OfflineInputGate(this.fakeParty, dim, owner_id);
        this.gates.add(gate);
        return gate;
    }

    public OfflineScalingGate scaling(OfflineGate inputX, BigInteger scalingFactor) {
        OfflineScalingGate gate = new OfflineScalingGate(inputX, scalingFactor);
        this.gates.add(gate);
        return gate;
    }

    public OfflineAddGate add(OfflineGate inputX, OfflineGate inputY) {
        OfflineAddGate gate = new OfflineAddGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public OfflineDivisionGate divide(OfflineGate inputX) {
        OfflineDivisionGate gate = new OfflineDivisionGate(inputX);
        this.gates.add(gate);
        return gate;
    }

    public OfflineDivisionGate divide(OfflineGate inputX, OfflineOutputGate inputY) {
        OfflineDivisionGate gate = new OfflineDivisionGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public OfflineLogGate log(OfflineGate inputX) {
        OfflineLogGate gate = new OfflineLogGate(inputX);
        this.gates.add(gate);
        return gate;
    }

    public OfflineReduceGate reduce(OfflineGate inputX) {
        OfflineReduceGate gate = new OfflineReduceGate(inputX);
        this.gates.add(gate);
        return gate;
    }


    public OfflineCombinationGate combination(OfflineGate... inputGates) {
        OfflineCombinationGate gate = new OfflineCombinationGate(inputGates);
        this.gates.add(gate);
        return gate;
    }

    public OfflineDotProductGate dotProduct(OfflineGate inputX, OfflineGate inputY) {
        OfflineDotProductGate gate = new OfflineDotProductGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public OfflineDotProdWithFilterGate dotProdWithFilter(
            OfflineGate inputX, OfflineGate inputY) {
        OfflineDotProdWithFilterGate gate = new OfflineDotProdWithFilterGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public OfflineElemMulThenDivGate elemMulThenDiv(OfflineGate inputX, OfflineGate inputY,
            OfflineOutputGate divisorGate) {
        OfflineElemMulThenDivGate gate = new OfflineElemMulThenDivGate(inputX, inputY, divisorGate);
        this.gates.add(gate);
        return gate;
    }

    public OfflineDotProdThenDivGate dotProdThenDivGate(List<OfflineGate> inputXGates,
            List<OfflineGate> inputYGates,
            OfflineOutputGate divisorGate) {
        List<OfflineGate> input = new ArrayList<>();
        input.addAll(inputXGates);
        input.addAll(inputYGates);
        input.add(divisorGate);
        OfflineDotProdThenDivGate gate = new OfflineDotProdThenDivGate(input);
        this.gates.add(gate);
        return gate;
    }


    public OfflineSubtractGate subtract(OfflineGate inputX, OfflineGate inputY) {
        OfflineSubtractGate gate = new OfflineSubtractGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public OfflineOutputGate output(OfflineGate inputX) {
        OfflineOutputGate gate = new OfflineOutputGate(inputX);
        this.gates.add(gate);
        return gate;
    }

    public OfflineElemWiseMultGate elemWiseMult(OfflineGate inputX, OfflineGate inputY) {
        OfflineElemWiseMultGate gate = new OfflineElemWiseMultGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public OfflineElemWiseMultThenMulConstGate elemWiseMultThenMulConst(OfflineGate inputX,
            OfflineGate inputY) {
        OfflineElemWiseMultThenMulConstGate gate = new OfflineElemWiseMultThenMulConstGate(inputX, inputY);
        this.gates.add(gate);
        return gate;
    }

    public List<OfflineGate> getEndpoints() {
        return endpoints;
    }
}
