package nu.nethome.home.items;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class UsbScannerTest {

    private UsbScanner usbScanner;

    @Before
    public void setUp() throws Exception {
        usbScanner = new UsbScanner();
    }

    @Ignore
    @Test
    public void scanDevices() throws Exception {
        try {
            usbScanner.activate(null);
            usbScanner.scan();
        } finally {
            usbScanner.stop();
        }
    }
}
