package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;

import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class InitializePodServiceUnitTests {
    @Mock
    private OmnipodCommunicationService communicationService;

    @After
    public void tearDown() {
        verifyNoMoreInteractions(communicationService);
    }

    @Test
    public void testExecuteAssignAddressCommand() {
        // TODO
    }

    @Test
    public void testExecuteConfirmPairingCommand() {
        // TODO
    }

    @Test
    public void testExecuteConfigureLowReservoirAlertCommand() {
        // TODO
    }

    @Test
    public void testExecuteConfigureInsertionAlertCommand() {
        // TODO
    }

    @Test
    public void testExecutePrimeBolusCommand() {
        // TODO
    }

    // TODO add scenarios
}
