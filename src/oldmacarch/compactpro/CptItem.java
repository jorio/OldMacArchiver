package oldmacarch.compactpro;

import java.io.IOException;
import java.io.OutputStream;

public interface CptItem {
    public void writeHeader(OutputStream out) throws IOException;
}
