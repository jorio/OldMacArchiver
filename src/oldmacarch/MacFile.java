package oldmacarch;

import java.nio.charset.Charset;

public class MacFile extends MacItem {
    public MacFork rsrc;
    public MacFork data;

    public MacFile(MacFolder parent, MacFork rFork, MacFork dFork, MacFinf finf, Charset charset) {
        super(parent, dFork.file, finf, charset);

        rsrc = rFork;
        data = dFork;
    }

    public int concatenatedLength() {
        return rsrc.length + data.length;
    }
}