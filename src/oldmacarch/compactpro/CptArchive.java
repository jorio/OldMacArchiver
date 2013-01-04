package oldmacarch.compactpro;

import oldmacarch.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class CptArchive extends Archive {
    private final byte[] comment;
    private ArrayList<CptItem> items;
    private long sizeEstimate;

    protected final static FourCC TYPE_4CC = new FourCC("PACT");
    protected final static FourCC CREATOR_4CC = new FourCC("CPCT");
    private final static int ARCHIVE_HEADER_LENGTH = 8;
    private final static int MAX_COMMENT_LENGTH = 255;
    private final static int MAX_ARCHIVE_LENGTH = Integer.MAX_VALUE;
    private final static int MAX_FILES_IN_ARCHIVE = 1500;

    public CptArchive(Charset macCharset, String commentString) {
        super(macCharset);

        comment = commentString.getBytes(charset);

        if (comment.length > MAX_COMMENT_LENGTH)
            throw new IllegalArgumentException("The archive comment cannot exceed "
                    + MAX_COMMENT_LENGTH + " bytes");

        items = new ArrayList<CptItem>();
    }

    public FourCC getNativeType() {
        return TYPE_4CC;
    }

    public FourCC getNativeCreator() {
        return CREATOR_4CC;
    }

    public String getHumanArchiveType() {
        return "Compact Pro compatible";
    }

    public void add(MacItem i) {
        if (items.size() == MAX_FILES_IN_ARCHIVE) {
            throw new RuntimeException("Archives can't contain more than "
                + MAX_FILES_IN_ARCHIVE + " files or folders.");
        }

        items.add((CptItem)i);

        if (i instanceof MacFile) {
            sizeEstimate += ((MacFile)i).concatenatedLength();
            if (sizeEstimate >= MAX_ARCHIVE_LENGTH)
                throw new RuntimeException("Archives can't exceed 2 GB.");
        }
    }

    public MacFolder makeFolder(MacFolder parent, File directory, MacFinf finf) {
        return new CptFolder(parent, directory, finf, charset);
    }

    public MacFile makeFile(MacFolder parent, MacFork rsrc, MacFork data, MacFinf finf) {
        return new CptFile(parent, rsrc, data, finf, charset);
    }

    private void writeArchiveHeader(OutputStream out) throws IOException {
        DataOutputStream w = new DataOutputStream(out);

        w.writeShort(items.size());
        w.writeByte(comment.length);
        w.write(comment);

        for (CptItem i: items)
            i.writeHeader(w);
    }

    public int write(File outFile, int skip) throws IOException {
        int archiveLength = 0;
        int fileHeaderOffset = ARCHIVE_HEADER_LENGTH;

        FileOutputStream fos = new FileOutputStream(outFile);
        BufferedOutputStream w = new BufferedOutputStream(fos);

        IOUtil.pad(w, skip);

        w.write(new byte[]{1,1,0,0, 0,0,0,0});

        for (CptItem i: items) {
            System.out.println(((MacItem) i).getPath());
            if (i instanceof CptFile)
                fileHeaderOffset += ((CptFile)i).encodeData(w, fileHeaderOffset);
        }

        w.close();
        fos.close();

        RandomAccessFile raf = new RandomAccessFile(outFile, "rw");
        raf.seek(skip + 4);
        raf.writeInt(fileHeaderOffset);
        raf.seek(skip + fileHeaderOffset);

        ByteArrayOutputStream headerBAOS = new ByteArrayOutputStream();
        CptCrc32 headerCRC = new CptCrc32(headerBAOS);
        writeArchiveHeader(headerCRC);
        raf.writeInt(headerCRC.checksum());
        raf.write(headerBAOS.toByteArray());

        archiveLength = (int)raf.getFilePointer() - skip;

        raf.close();

        return archiveLength;
    }
}
