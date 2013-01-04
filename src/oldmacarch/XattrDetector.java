package oldmacarch;

import java.io.*;

public abstract class XattrDetector {
    public abstract MacFinf detectFinderInfo(File dataFork) throws IOException;
    public abstract MacFork detectResourceFork(File dataFork) throws IOException;
    protected abstract boolean shouldSkip(File file);
}
