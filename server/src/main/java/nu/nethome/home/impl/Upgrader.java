package nu.nethome.home.impl;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 */
public class Upgrader {

    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    private static final int UNZIP_BUFFER_SIZE = 4096;
    private static final String RELEASE_ZIP = "release.zip";

    private volatile long downloadedBytes = 0;
    private volatile long unzippedBytes = 0;

    public File downloadRelease(String url) throws IOException {
        File downloadDirectory = createTempDirectory("NetHomeUpgrade");
        String localFilename = downloadDirectory.getAbsolutePath() + File.separator + RELEASE_ZIP;
        downloadFromUrl(new URL(url), localFilename);
        extract(new File(localFilename), downloadDirectory);
        return null;
    }

    private static File createTempDirectory(String baseName) throws IOException {
        final File temp;

        temp = File.createTempFile(baseName, Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    void downloadFromUrl(URL url, String localFilename) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URLConnection urlConn = url.openConnection();//connect

            is = urlConn.getInputStream();               //get connection inputstream
            fos = new FileOutputStream(localFilename);   //open outputstream to local file

            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];              //declare 4KB buffer
            int len;

            //while we have availble data, continue downloading and storing to local file
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                downloadedBytes += len;
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private void extractFile(ZipInputStream in, File outdir, String name) throws IOException {
        byte[] buffer = new byte[UNZIP_BUFFER_SIZE];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
        int count = -1;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
            unzippedBytes += count;
        }
        out.close();
    }

    private static void mkdirs(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists())
            d.mkdirs();
    }

    private static String dirpart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }

    /**
     * Extract zipfile to outdir with complete directory structure
     *
     * @param zipfile Input .zip file
     * @param outdir  Output directory
     */
    public void extract(File zipfile, File outdir) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
            ZipEntry entry;
            String name, dir;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                if (entry.isDirectory()) {
                    mkdirs(outdir, name);
                    continue;
                }
        /* this part is necessary because file entry can come before
         * directory entry where is file located
         * i.e.:
         *   /foo/foo.txt
         *   /foo/
         */
                dir = dirpart(name);
                if (dir != null)
                    mkdirs(outdir, dir);

                extractFile(zin, outdir, name);
            }
            zin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
