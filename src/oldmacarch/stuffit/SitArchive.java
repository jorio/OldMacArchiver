package oldmacarch.stuffit;

import oldmacarch.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

// Spec: http://code.google.com/p/theunarchiver/wiki/StuffItFormat

public class SitArchive extends Archive {
    private ArrayList<SitItem> topLevel;
    private long archiveSize;

    protected final static FourCC TYPE_4CC = new FourCC("SIT!");
    protected final static FourCC CREATOR_4CC = new FourCC("SIT!");
    protected final static int MAX_ARCHIVE_LENGTH = Integer.MAX_VALUE;
    protected final static int ARCHIVE_HEADER_LENGTH = 22;
    protected final static int FILE_HEADER_LENGTH = 112;
    protected final static int MAX_TOP_LEVEL_FILES = 65536;

    public SitArchive(Charset charset) {
        super(charset);
        topLevel = new ArrayList<SitItem>();
        archiveSize = ARCHIVE_HEADER_LENGTH;
    }

    public String getHumanArchiveType() {
        return "StuffIt 1.5.1 compatible";
    }

    public FourCC getNativeType() {
        return TYPE_4CC;
    }

    public FourCC getNativeCreator() {
        return CREATOR_4CC;
    }

    public void add(MacItem i) {
        if (i.parent == null) {
            if (topLevel.size() == MAX_TOP_LEVEL_FILES)
                throw new RuntimeException("There can only be up to " +
                    MAX_TOP_LEVEL_FILES + " top-level files.");
            topLevel.add((SitItem)i);
        }

        archiveSize += ((SitItem)i).sizeInArchive(true);

        if (archiveSize >= MAX_ARCHIVE_LENGTH)
            throw new RuntimeException("Archives can't exceed 2 GB.");
    }

    public int write(File outFile, int skip) throws IOException {
        int contentLength = 0;

        FileOutputStream fos = new FileOutputStream(outFile);
        BufferedOutputStream buffered = new BufferedOutputStream(fos);
        DataOutputStream w = new DataOutputStream(buffered);

        IOUtil.pad(w, skip);

        w.writeBytes("SIT!");
        w.writeShort(topLevel.size());
        w.writeInt((int)archiveSize);
        w.writeBytes("rLau");
        w.writeByte(1);
        IOUtil.pad(w, 7);

        for (SitItem item: topLevel)
            item.writeFirstPass(buffered);

        w.close();
        buffered.close();
        fos.close();

        RandomAccessFile raf = new RandomAccessFile(outFile, "rw");
        raf.seek(skip + ARCHIVE_HEADER_LENGTH);
        for (SitItem item: topLevel)
            item.writeSecondPass(raf);

        contentLength = (int)raf.getFilePointer() - skip;

        raf.close();

        return contentLength;
    }

    protected MacFolder makeFolder(MacFolder parent, File directory, MacFinf finf) {
        return new SitFolder(parent, directory, finf, charset);
    }

    protected MacFile makeFile(MacFolder parent, MacFork rsrc, MacFork data, MacFinf finf) {
        return new SitFile(parent, rsrc, data, finf, charset);
    }
}
