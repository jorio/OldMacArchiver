package oldmacarch;

import java.io.*;

public class MacFork {
    public final File file;
    private final long offset;
    public final int length;

    public MacFork(File f) {
        file = f;
        offset = 0;
        if (file != null) length = (int)file.length();
        else length = 0;
    }

    public MacFork(File f, long o, int l) {
        file = f;
        offset = o;
        length = l;
    }

    public MacFork(MacFork fork) {
        file = fork.file;
        offset = fork.offset;
        length = fork.length;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public InputStream open() throws IOException {
        FileInputStream fis = new FileInputStream(file);

        if (offset != fis.skip(offset))
            throw new IOException("couldn't skip enough bytes");

        return new BufferedInputStream(new FixedLengthInputStream(fis, length));
    }
}
