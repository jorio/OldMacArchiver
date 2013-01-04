package oldmacarch.stuffit;

import oldmacarch.*;

import java.io.*;
import java.nio.charset.Charset;

public class SitFile extends MacFile implements SitItem {
    class SitFork extends MacFork {
        short checksum;

        public SitFork(MacFork source) {
            super(source);
            checksum = 0;
        }
    }

    public SitFile(MacFolder parent, MacFork rFork, MacFork dFork, MacFinf finf, Charset charset) {
        super(parent, rFork, dFork, finf, charset);

        rsrc = new SitFork(rsrc);
        data = new SitFork(data);
    }

    private int encodeData(OutputStream out) throws IOException {
        SitCrc16 crcPassThru = new SitCrc16(out);

        SitFork[] sitForks = new SitFork[]{(SitFork)rsrc, (SitFork)data};

        for (SitFork fork: sitForks) {
            if (fork.isEmpty())
                continue;

            crcPassThru.resetChecksum();
            InputStream in = fork.open();

            int b;
            while (-1 != (b = in.read()))
                crcPassThru.write(b);

            in.close();

            fork.checksum = (short)(crcPassThru.checksum() & 0xffff);
        }

        return rsrc.length + data.length;
    }

    public void writeFirstPass(OutputStream out) throws IOException {
        System.out.println(getPath());

        // header will be written during the second pass, for now just write zeroes
        IOUtil.pad(out, SitArchive.FILE_HEADER_LENGTH);

        encodeData(out);
    }

    public void writeSecondPass(RandomAccessFile raf) throws IOException {
        ByteArrayOutputStream headerBAOS = new ByteArrayOutputStream(SitArchive.FILE_HEADER_LENGTH);
        SitCrc16 headerCrcPassThru = new SitCrc16(headerBAOS);
        DataOutputStream w = new DataOutputStream(headerCrcPassThru);

        w.writeShort(0);    // no compression for either fork
        w.writeByte(name.length);
        w.write(name);
        IOUtil.pad(w, 63 - name.length);
        w.writeInt(finf.type.value);
        w.writeInt(finf.creator.value);
        w.writeShort(finf.flags);
        w.writeInt(ctime);
        w.writeInt(mtime);
        for (int i = 0; i < 2; i++) {
            w.writeInt(rsrc.length);
            w.writeInt(data.length);
        }
        w.writeShort(((SitFork)rsrc).checksum);
        w.writeShort(((SitFork)data).checksum);
        IOUtil.pad(w, 6);
        w.writeShort(headerCrcPassThru.checksum());

        w.close();
        raf.write(headerBAOS.toByteArray());

        int skip = concatenatedLength();
        if (skip != raf.skipBytes(skip)) {
            throw new RuntimeException("couldn't skip enough bytes");
        }
    }

    public int sizeInArchive(boolean withHeaders) {
        return concatenatedLength() +
                (withHeaders? SitArchive.FILE_HEADER_LENGTH: 0);
    }
}

