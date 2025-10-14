package offline;

import org.junit.Test;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;


public class TestSubtractOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
//        FakeGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.subtract(a, b);
//        FakeGate e = fakeCircuit.subtract(d, c);
        OfflineGate f = fakeCircuit.output(d);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }

}
