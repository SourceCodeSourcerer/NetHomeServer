package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.items.net.ArpScanner;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 */
public class ArpScannerTest {

    private static final String SCAN_RESPONSE1 = "Interface: eth0, datalink type: EN10MB (Ethernet)\n" +
            "Starting arp-scan 1.8.1 with 256 hosts (http://www.nta-monitor.com/tools/arp-scan/)\n" +
            "192.168.1.1     e4:f4:c6:09:2c:26\n" +
            "192.168.1.4     20:d5:bf:02:bd:ab\n" +
            "192.168.1.2     9c:20:7b:e0:08:70\n" +
            "192.168.1.9     24:0a:64:10:4a:2d\n" +
            "192.168.1.100   00:1f:d0:58:3c:e9\n" +
            "192.168.1.120   00:17:88:18:ce:ee\n" +
            "192.168.1.7     e0:c9:7a:38:fc:79\n" +
            "192.168.1.5     80:ea:96:1f:03:b5\n" +
            "\n" +
            "10 packets received by filter, 0 packets dropped by kernel\n" +
            "Ending arp-scan 1.8.1: 256 hosts scanned in 2.112 seconds (121.21 hosts/sec). 8 responded";

    private static final String SCAN_RESPONSE2 = "Interface: eth0, datalink type: EN10MB (Ethernet)\n" +
            "Starting arp-scan 1.8.1 with 256 hosts (http://www.nta-monitor.com/tools/arp-scan/)\n" +
            "192.168.1.1     e4:f4:c6:09:2c:26       (Unknown)\n" +
            "192.168.1.4     20:d5:bf:02:bd:ab       (Unknown)\n" +
            "192.168.1.7     e0:c9:7a:38:fc:79       (Unknown)\n" +
            "192.168.1.6     74:e1:b6:bd:3f:18       (Unknown)\n" +
            "192.168.1.9     24:0a:64:10:4a:2d       (Unknown)\n" +
            "192.168.1.100   00:1f:d0:58:3c:e9       GIGA-BYTE TECHNOLOGY CO.,LTD.\n" +
            "192.168.1.120   00:17:88:18:ce:ee       Philips Lighting BV\n" +
            "192.168.1.5     80:ea:96:1f:03:b5       (Unknown)\n" +
            "\n" +
            "10 packets received by filter, 0 packets dropped by kernel\n" +
            "Ending arp-scan 1.8.1: 256 hosts scanned in 4.033 seconds (63.48 hosts/sec). 8 responded";

    private ArpScanner scanner;
    private HomeService server;
    private InternalEvent sentEvent;
    private LocalHomeItemProxy itemProxy;

    @Before
    public void setUp() throws Exception {
        scanner = spy(new ArpScanner());
        itemProxy = new LocalHomeItemProxy(scanner);
        sentEvent = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(sentEvent).when(server).createEvent(anyString(), anyString());
    }

    @After
    public void tearDown() throws Exception {
        scanner.stop();
    }

    @Test
    public void countAttributeEmptyBeforeScan() throws Exception {
        assertThat(itemProxy.getAttributeValue("MacCount"), is(""));
    }

    @Test
    public void countAttributeCountsMacAddresses() throws Exception {
        scanner.activate(server);
        doReturn(Arrays.asList("e4:f4:c6:09:2c:26", "80:ea:96:1f:03:b5")).when(scanner).scan();

        scanner.reportScanResult();

        assertThat(itemProxy.getAttributeValue("MacCount"), is("2"));
    }

    @Test
    public void scansOnSpecifiedInterval() throws Exception {
        doReturn(Arrays.asList("e4:f4:c6:09:2c:26", "80:ea:96:1f:03:b5")).when(scanner).scan();
        scanner.scanInterval = 50L;
        scanner.activate(server);

        Thread.sleep(200L);

        verify(server, atLeast(2)).send(sentEvent);
        verify(server, atMost(4)).send(sentEvent);
    }

    @Test
    public void reportsError() throws Exception {
        scanner.activate(server);
        doThrow(new ExecutionFailure("Test")).when(scanner).scan();

        scanner.reportScanResult();

        verify(server, times(0)).send(sentEvent);
        assertThat(itemProxy.getAttributeValue("MacCount"), is("Test"));
    }

    @Test
    public void sendsEvent() throws Exception {
        scanner.activate(server);
        doReturn(Arrays.asList("e4:f4:c6:09:2c:26", "80:ea:96:1f:03:b5")).when(scanner).scan();

        scanner.reportScanResult();

        verify(server).createEvent(ArpScanner.ARP_SCAN_MESSAGE, "e4:f4:c6:09:2c:26,80:ea:96:1f:03:b5");
        verify(server).send(sentEvent);
    }

    @Test
    public void canSetInterval() throws Exception {
        itemProxy.setAttributeValue("ScanInterval", "60");
        assertThat(itemProxy.getAttributeValue("ScanInterval"), is("60"));
        assertThat(scanner.scanInterval, is(60000L));
    }

    @Test
    public void canParseMACFromOutput() throws Exception {
        InputStream is = new ByteArrayInputStream(SCAN_RESPONSE1.getBytes());
        ArpScanner.ResponseParser parser = new ArpScanner.ResponseParser(is);
        parser.start();
        parser.join();
        assertThat(parser.responseLines.size(), is(8));
        assertThat(parser.responseLines.get(0), is("e4:f4:c6:09:2c:26"));
        assertThat(parser.responseLines.get(1), is("20:d5:bf:02:bd:ab"));
        assertThat(parser.responseLines.get(7), is("80:ea:96:1f:03:b5"));
    }
}
