package offline;

import org.junit.Test;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;


public class TestAdditionOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
        OfflineGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.add(a, b);
        OfflineGate e = fakeCircuit.add(d, c);
        OfflineGate f = fakeCircuit.output(e);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }

}
