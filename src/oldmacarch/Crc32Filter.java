package oldmacarch;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Crc32Filter extends FilterOutputStream {
    private final int[] table;
    private final int startCrc;
    private int crc;

    protected Crc32Filter(OutputStream out, int[] table, int start) {
        super(out);
        this.table = table;
        this.startCrc = start;
        this.crc = startCrc;
    }

    @Override public void write(int b) throws IOException {
        crc = table[(crc^b)&0xff]^(crc>>>8);
        out.write(b);
    }

    public int checksum() {
        return crc;
    }

    public void resetChecksum() {
        crc = startCrc;
    }
}
