package oldmacarch.detector;

import oldmacarch.MacFinf;
import oldmacarch.MacFork;
import oldmacarch.XattrDetector;

import java.io.*;
import java.util.HashMap;

// Spec: http://web.archive.org/web/20110719072636/http://users.phg-online.de/tk/netatalk/doc/Apple/v2/AppleSingle_AppleDouble.pdf
// Older spec: http://www.ee.columbia.edu/~dpwe/resources/FTN.e000023.htm

public class AppleDouble extends XattrDetector {
    protected static File auxiliary(File dataFork) {
        return new File(dataFork.getParent(), "._" + dataFork.getName());
    }

    static class Aux {
        static class Entry {
            long offset;
            int length;
        }

        File data;
        File aux;
        HashMap<Integer, Entry> entries;

        Aux(File d, File a) {
            data = d;
            aux = a;
            entries = new HashMap<Integer, Entry>();
        }
    }

    private Aux cache;

    private void prepCache(File dataFork) throws IOException {
        if (cache != null && cache.data.equals(dataFork))
            return;

        File auxFile = auxiliary(dataFork);
        cache = new Aux(dataFork, auxFile);
        int tmp;
        int entryCount;
        RandomAccessFile raf;

        raf = new RandomAccessFile(auxFile, "r");

        tmp = raf.readInt();
        if (tmp != 0x00051607)
            throw new IllegalArgumentException(String.format(
                    "AppleDouble magic number mismatch $%08X", tmp));

        tmp = raf.readInt();
        if (tmp != 0x00020000)
            throw new IllegalArgumentException(String.format(
                    "AppleDouble unknown version $%08X", tmp));

        raf.seek(4+4+16);

        entryCount = raf.readShort() & 0xffff;

        for (int i = 0; i < entryCount; i++) {
            int id = raf.readInt();
            Aux.Entry entry = new Aux.Entry();
            entry.offset = raf.readInt();
            entry.length = raf.readInt();
            cache.entries.put(id, entry);
        }

        raf.close();
    }

    public MacFinf detectFinderInfo(File dataFork) throws IOException {
        try {
            prepCache(dataFork);
        } catch (IOException ex) {
            return null;
        }

        RandomAccessFile raf = new RandomAccessFile(cache.aux, "r");
        Aux.Entry entry9 = cache.entries.get(9);
        if (entry9 == null)
            return null;

        byte[] buf = new byte[entry9.length];
        raf.seek(entry9.offset);
        raf.read(buf, 0, entry9.length);
        raf.close();
        InputStream bufBAIS = new ByteArrayInputStream(buf);
        return new MacFinf(dataFork.isFile(), bufBAIS);
    }

    public MacFork detectResourceFork(File dataFork) throws IOException {
        prepCache(dataFork);

        if (cache == null)
            return null;

        Aux.Entry entry2 = cache.entries.get(2);
        if (entry2 == null)
            return null;

        return new MacFork(cache.aux, entry2.offset, entry2.length);
    }

    protected boolean shouldSkip(File file) {
        return file.getName().startsWith("._");
    }
}
