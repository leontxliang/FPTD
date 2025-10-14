package offline;

import org.junit.Test;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;

public class TestElemWiseMulThenMulConstOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.elemWiseMultThenMulConst(a, b);
        OfflineGate f = fakeCircuit.output(d);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }
}
