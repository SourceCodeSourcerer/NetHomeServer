package nu.nethome.home.items.jeelink;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class JeeLinkProtocolPortTest {

    @Ignore
    @Test
    public void testReadingVersion() throws Exception {

        JeeLinkProtocolPort port = new JeeLinkProtocolPort("COM7", null);

        Thread.sleep(30000);

        port.getReportedVersion();

        port.close();

        Thread.sleep(5000);
    }

}
