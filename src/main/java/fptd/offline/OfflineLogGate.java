package fptd.offline;

public class OfflineLogGate extends OfflineGate {

    private OfflineCircuit fakeCircuit;

    public OfflineLogGate(OfflineGate inputX) {
        super(inputX, null);
        this.fakeCircuit = new OfflineCircuit(this.fakeParty);
        this.dim = inputX.dim;

        OfflineGate gateMul = this.fakeCircuit.elemWiseMult(this.firstGate(), this.firstGate());
        OfflineGate divFixed = this.fakeCircuit.divide(gateMul);
        this.fakeCircuit.addEndpoint(divFixed);
    }

    @Override
    void doRunOffline() {
        this.fakeCircuit.runOffline();
        this.lambda_clear_list = this.fakeCircuit.getEndpoints().getFirst().lambda_clear_list;
        this.lambda_shr_matrix = this.fakeCircuit.getEndpoints().getFirst().lambda_shr_matrix;
    }
}
