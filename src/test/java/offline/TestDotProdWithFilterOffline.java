package offline;

import org.junit.Test;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.FakeParty;

import static fptd.Params.N;


public class TestDotProdWithFilterOffline {

    @Test
    public void testFakeCircuit(){
        System.out.println(Long.MAX_VALUE);

        FakeParty fakeParty = new FakeParty("test", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);

        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
//        FakeGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.dotProdWithFilter(a, b);

//        FakeGate e = fakeCircuit.scaling(d, Params.ROUND_FOR_DIVISION1);

//        FakeGate e = fakeCircuit.add(d, c);
        OfflineGate f = fakeCircuit.output(d);
        fakeCircuit.addEndpoint(f);
        fakeCircuit.runOffline();
    }

}
