package oldmacarch.compactpro;

import oldmacarch.MacFile;
import oldmacarch.MacFinf;
import oldmacarch.MacFolder;
import oldmacarch.MacFork;

import java.io.*;
import java.nio.charset.Charset;

public class CptFile extends MacFile implements CptItem {
    class CptFork extends MacFork {
        int encodedLength;

        public CptFork(MacFork source) {
            super(source);
            encodedLength = 0;
        }
    }

    int dataOffset;
    int checksum;

    public CptFile(MacFolder parent, MacFork rFork, MacFork dFork, MacFinf finf, Charset charset) {
        super(parent, rFork, dFork, finf, charset);
        dataOffset = -1;
        checksum = -1;

        rsrc = new CptFork(rsrc);
        data = new CptFork(data);
    }

    public int encodeData(OutputStream out, int currentOffset) throws IOException {
        dataOffset = currentOffset;

        CptCrc32 crcPassThru = new CptCrc32(out);

        CptFork[] cptForks = new CptFork[]{(CptFork)rsrc, (CptFork)data};

        for (CptFork fork: cptForks) {
            if (fork.isEmpty())
                continue;

            InputStream in = fork.open();

            int b;
            int previous = -1;

            while (-1 != (b = in.read())) {
                crcPassThru.write(b);
                fork.encodedLength++;

                if (b == 0x82 && previous == 0x81) {
                    out.write(0);
                    fork.encodedLength++;
                }

                previous = b;
            }

            in.close();
        }

        checksum = crcPassThru.checksum();
        return cptForks[0].encodedLength + cptForks[1].encodedLength;
    }

    public void writeHeader(OutputStream out) throws IOException {
        DataOutputStream w = new DataOutputStream(out);

        w.writeByte(name.length);       // name length
        w.write(name);                  // name
        w.writeByte(1);                 // volume number
        w.writeInt(dataOffset);
        w.writeInt(finf.type.value);
        w.writeInt(finf.creator.value);
        w.writeInt(ctime);              // creation date
        w.writeInt(mtime);              // modification date
        w.writeShort(finf.flags);       // finder flags
        w.writeInt(checksum);           // CRC of concatenation of both forks (rsrc then data)
        w.writeShort(0);                // file flags - zero because uncompressed and unencrypted
        w.writeInt(rsrc.length);        // rsrc uncompressed length
        w.writeInt(data.length);        // data uncompressed length
        w.writeInt(((CptFork)rsrc).encodedLength); // rsrc 'compressed' length
        w.writeInt(((CptFork)data).encodedLength); // data 'compressed' length
    }
}
