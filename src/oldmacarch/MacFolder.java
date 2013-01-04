package oldmacarch;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MacFolder extends MacItem {
    private int subChildCount;
    protected ArrayList<MacItem> immediateChildren;

    public MacFolder(MacFolder parent, File directory, MacFinf finf, Charset charset) {
        super(parent, directory, finf, charset);
        subChildCount = 0;
        immediateChildren = new ArrayList<MacItem>();
    }

    public void newSubChild(MacItem child) {
        subChildCount++;

        if (parent != null)
            parent.newSubChild(child);

        if (child.parent == this)
            immediateChildren.add(child);
    }

    public int getSubChildCount() {
        return subChildCount;
    }
}
