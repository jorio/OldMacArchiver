package oldmacarch.compactpro;

import oldmacarch.MacFinf;
import oldmacarch.MacFolder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class CptFolder extends MacFolder implements CptItem {
    public CptFolder(MacFolder parent, File directory, MacFinf finf, Charset charset) {
        super(parent, directory, finf, charset);
    }

    public void writeHeader(OutputStream out) throws IOException {
        DataOutputStream w = new DataOutputStream(out);
        w.write(name.length | 0x80);
        w.write(name);
        w.writeShort(getSubChildCount());
    }
}
