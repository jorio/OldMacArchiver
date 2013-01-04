package oldmacarch.stuffit;

import oldmacarch.IOUtil;
import oldmacarch.MacFinf;
import oldmacarch.MacFolder;
import oldmacarch.MacItem;

import java.io.*;
import java.nio.charset.Charset;

public class SitFolder extends MacFolder implements SitItem {
    public SitFolder(MacFolder parent, File directory, MacFinf finf, Charset charset) {
        super(parent, directory, finf, charset);
    }

    private void writeHeader(OutputStream out, boolean opening) throws IOException {
        SitCrc16 crc = new SitCrc16(out);
        DataOutputStream w = new DataOutputStream(crc);

        short compressionMethod;
        int uncompressedLength;
        int compressedLength;

        if (opening) {
            compressionMethod = 0x2020;
            uncompressedLength = sizeInArchive(false);
            compressedLength = sizeInArchive(true) - SitArchive.FILE_HEADER_LENGTH;
        } else {
            compressionMethod = 0x2121;
            uncompressedLength = 0;
            compressedLength = 0;
        }

        w.writeShort(compressionMethod);
        w.writeByte(name.length);
        w.write(name);
        IOUtil.pad(w, 63-name.length); // name padding
        w.writeInt(finf.type.value);
        w.writeInt(finf.creator.value);
        w.writeShort(finf.flags);
        w.writeInt(ctime);
        w.writeInt(mtime);
        w.writeInt(0);              // resource fork uncompressed length
        w.writeInt(uncompressedLength);
        w.writeInt(0);              // resource fork compressed length
        w.writeInt(compressedLength);
        IOUtil.pad(w, 2 * 2 + 6) ; // CRC for both forks, then 6 reserved bytes
        w.writeShort(crc.checksum());
    }

    public void writeFirstPass(OutputStream out) throws IOException {
        System.out.println(getPath() + " (" + finf + ")");

        writeHeader(out, true);

        for (MacItem i: immediateChildren)
            ((SitItem)i).writeFirstPass(out);

        writeHeader(out, false);
    }

    public void writeSecondPass(RandomAccessFile raf) throws IOException {
        int skip = SitArchive.FILE_HEADER_LENGTH;

        if (skip != raf.skipBytes(skip))
            throw new RuntimeException("couldn't skip enough bytes");

        for (MacItem i: immediateChildren)
            ((SitItem)i).writeSecondPass(raf);

        if (skip != raf.skipBytes(skip))
            throw new RuntimeException("couldn't skip enough bytes");
    }

    public int sizeInArchive(boolean withHeaders) {
        int sum = withHeaders? 2*SitArchive.FILE_HEADER_LENGTH: 0;

        for (MacItem item: immediateChildren)
            sum += ((SitItem)item).sizeInArchive(withHeaders);

        return sum;
    }
}
