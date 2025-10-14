package offline;

import org.junit.Test;
import fptd.offline.OfflineCircuit;
import fptd.offline.OfflineGate;
import fptd.offline.OfflineOutputGate;
import fptd.offline.FakeParty;

import java.util.List;

import static fptd.Params.N;


public class TestDotProdThenDivOffline {

    @Test
    public void testFakeCircuit(){
        FakeParty fakeParty = new FakeParty("test_dot_prod_then_div", N);
        OfflineCircuit fakeCircuit = new OfflineCircuit(fakeParty);
        OfflineGate a = fakeCircuit.input(0, 2);
        OfflineGate b = fakeCircuit.input(0, 2);
        OfflineGate c = fakeCircuit.input(0, 2);
        OfflineGate d = fakeCircuit.input(0, 2);

        OfflineGate e = fakeCircuit.input(0, 2);

        OfflineGate f = fakeCircuit.dotProdThenDivGate(List.of(a, b), List.of(c, d), new OfflineOutputGate(e));
        OfflineGate g = fakeCircuit.output(f);
        fakeCircuit.addEndpoint(g);
        fakeCircuit.runOffline();
    }

}
