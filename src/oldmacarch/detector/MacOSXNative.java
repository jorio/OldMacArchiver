package oldmacarch.detector;

import oldmacarch.MacFinf;
import oldmacarch.MacFork;
import oldmacarch.XattrDetector;

import java.io.*;

/*
Sample xattr response (I couldn't get it to output binary data directly):

41 50 50 4C 55 53 49 54 21 00 00 00 00 00 00 00
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
*/


public class MacOSXNative extends XattrDetector {
    public MacFinf detectFinderInfo(File dataFork) throws IOException {
        Runtime runtime = Runtime.getRuntime();

        Process xattr = runtime.exec(new String[] {
                "xattr", "-p", "-x", "com.apple.FinderInfo", dataFork.getAbsolutePath() });

        int exitValue;

        try {
            exitValue = xattr.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException("interrupted while waiting for xattr to complete");
        }

        if (exitValue == 0) {
            BufferedReader buf = new BufferedReader(new InputStreamReader(xattr.getInputStream()));
            String line;
            ByteArrayOutputStream finfBAOS = new ByteArrayOutputStream();

            while ((line = buf.readLine()) != null) {
                for (String hexByte: line.split(" "))
                    finfBAOS.write(Integer.parseInt(hexByte, 16));
            }

            finfBAOS.close();
            buf.close();

            ByteArrayInputStream finfBAIS = new ByteArrayInputStream(finfBAOS.toByteArray());
            return new MacFinf(dataFork.isFile(), finfBAIS);
        } else {
            return null;
        }
    }

    public MacFork detectResourceFork(File dataFork) {
        File rsrcFile = new File(dataFork, "..namedfork/rsrc");
        return new MacFork(rsrcFile);
    }

    protected boolean shouldSkip(File file) {
        return false;
    }
}
