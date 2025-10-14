package offline;

import org.junit.Test;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;


public class TestLogOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test_log", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
//        FakeGate b = fakeCircuit.input(0, 2);
//        FakeGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.log(a);
//        FakeGate e = fakeCircuit.add(d, c);
        OfflineGate f = fakeCircuit.output(d);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }


}
