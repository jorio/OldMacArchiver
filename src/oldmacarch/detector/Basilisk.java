package oldmacarch.detector;

import oldmacarch.MacFinf;
import oldmacarch.MacFork;
import oldmacarch.XattrDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Basilisk extends XattrDetector {
    private static File auxiliary(File f, String dir) {
        File auxDir = new File(f.getParent(), dir);
        return new File(auxDir, f.getName());
    }

    public MacFinf detectFinderInfo(File dataFork) throws IOException {
        File aux = auxiliary(dataFork, ".finf");
        return new MacFinf(dataFork.isFile(), new FileInputStream(aux));
    }

    public MacFork detectResourceFork(File dataFork) {
        File aux = auxiliary(dataFork, ".rsrc");
        return new MacFork(aux);
    }

    protected boolean shouldSkip(File file) {
        if (!file.isDirectory())
            return false;

        String n = file.getName();
        return n.equals(".rsrc") || n.equals(".finf");
    }
}
