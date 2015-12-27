package nu.nethome.home.items.jeelink;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class JeeLinkPortTest {

    @Ignore
    @Test
    public void getVersion() throws Exception {
        JeeLinkPort jeeLinkPort = new JeeLinkPort();
        jeeLinkPort.readArduinoVersion("COM7");
    }
}
