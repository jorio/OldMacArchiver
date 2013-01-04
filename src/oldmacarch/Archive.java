package oldmacarch;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Calendar;

// MacBinary spec: http://files.stairways.com/other/macbinaryii-standard-info.txt

public abstract class Archive {
    protected Charset charset;

    public abstract void add(MacItem i);
    public abstract int write(File f, int skip) throws IOException;

    protected abstract MacFolder makeFolder(MacFolder parent, File directory, MacFinf finf);
    protected abstract MacFile makeFile(MacFolder parent, MacFork rsrc, MacFork data, MacFinf finf);

    protected abstract FourCC getNativeType();
    protected abstract FourCC getNativeCreator();

    public abstract String getHumanArchiveType();

    public Archive(Charset charset) {
        this.charset = charset;
    }

    public void add(File file, MacFolder parent, XattrDetector xtd) throws IOException {
        if (xtd.shouldSkip(file)) {
            System.out.println("### SKIPPING: " + file);
            return;
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }

        MacFinf finf = xtd.detectFinderInfo(file);
        MacFork dFork = new MacFork(file);
        MacFork rFork = file.isFile()? xtd.detectResourceFork(file): null;

        if (finf == null) finf = new MacFinf(file.isFile());
        if (rFork == null) rFork = new MacFork((File)null);

        if (file.isDirectory()) {
            MacFolder folder = makeFolder(parent, file, finf);
            add(folder);

            if (file.listFiles() != null) {
                for (File child: file.listFiles())
                    add(child, folder, xtd);
            }
        } else {
            add(makeFile(parent, rFork, dFork, finf));
        }

        System.out.print(".");
    }

    public void writeWithMacBinary1Wrapper(File outFile, String archiveName) throws IOException {
        byte[] archiveNameBytes = archiveName.getBytes(charset);

        int arcLen = write(outFile, 128);

        RandomAccessFile raf = new RandomAccessFile(outFile, "rw");

        // write name
        raf.seek(1);
        raf.write(archiveNameBytes.length);
        raf.write(archiveNameBytes);

        // type/creator
        raf.seek(65);
        raf.writeInt(getNativeType().value);
        raf.writeInt(getNativeCreator().value);

        // data fork length
        raf.seek(83);
        raf.writeInt(arcLen);

        // ctime/atime (required to be nonzero)
        int macTime = MacItem.macTime(Calendar.getInstance().getTimeInMillis());
        raf.seek(91);
        raf.writeInt(macTime);
        raf.writeInt(macTime);

        // pad data fork to multiple of 128
        raf.seek(128 + arcLen);
        int dfpad = (128 - (arcLen%128)) % 128;
        IOUtil.pad(raf, dfpad);

        raf.close();
    }

    public void write(File outFile) throws IOException {
        write(outFile, 0);
    }
}
