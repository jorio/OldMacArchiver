package oldmacarch;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class IOUtil {
    public static byte[] ZEROES = new byte[256];

    public static void pad(OutputStream out, int byteCount) throws IOException {
        out.write(ZEROES, 0, byteCount);
    }

    public static void pad(RandomAccessFile out, int byteCount) throws IOException {
        out.write(ZEROES, 0, byteCount);
    }
}
