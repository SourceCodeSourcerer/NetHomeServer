package nu.nethome.home.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class UpgraderTest {

    Upgrader upgrader;

    @Before
    public void setUp() throws Exception {
        upgrader = new Upgrader();
    }

    @Ignore
    @Test
    public void Download() throws Exception {

        upgrader.downloadRelease("http://wiki.nethome.nu/lib/exe/fetch.php/nethomeservernightly.zip");

    }
}
