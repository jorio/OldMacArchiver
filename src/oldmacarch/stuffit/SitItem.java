package oldmacarch.stuffit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public interface SitItem {
    public void writeFirstPass(OutputStream out) throws IOException;
    public void writeSecondPass(RandomAccessFile out) throws IOException;
    public int sizeInArchive(boolean withHeaders);
}
