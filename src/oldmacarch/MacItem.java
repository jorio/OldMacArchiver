package oldmacarch;

import java.io.File;
import java.nio.charset.Charset;

public abstract class MacItem {
    public final MacFolder parent;
    protected final byte[] name;
    private final String readableName;
    protected int ctime;
    protected int mtime;
    protected final MacFinf finf;

    private static final int MAX_FILENAME_LENGTH = 31;
    private static final int EPOCH_OFFSET = 2082844800;

    protected static int macTime(long millis) {
        return (int)(millis / 1000L + EPOCH_OFFSET);
    }

    private void copyTime(File timeReference) {
        mtime = macTime(timeReference.lastModified());
/*
        try {
            Path filepath = Paths.get(timeReference.getAbsolutePath());
            BasicFileAttributes attr = Files.readAttributes(filepath, BasicFileAttributes.class);
            ctime = macTime(attr.creationTime().toMillis());
        } catch (IOException ex) {
            System.err.println("ctime couldn't be read for file " + timeReference);
            ctime = mtime;
        }
*/
        ctime = mtime;
    }

    protected MacItem(MacFolder parentFolder, File itemFile, MacFinf finf, Charset charset) {
        parent = parentFolder;
        this.finf = finf;

        if (itemFile.getName().indexOf(':') >= 0) {
            readableName = itemFile.getName().replace(':', '/');
            System.err.println("WARNING: replacing colon with slash: " + readableName);
            //throw new IllegalArgumentException("File names can't contain " +
            //        "the colon character: " + readableName);
        } else {
            readableName = itemFile.getName();
        }

        name = readableName.getBytes(charset);

        copyTime(itemFile);

        if (parent != null) {
            parent.newSubChild(this);
        }

        if (name.length > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("File names can't exceed "
                    + MAX_FILENAME_LENGTH + " bytes: " + readableName);
        }

    }


    public String getName() {
        return readableName;
    }

    public String getPath() {
        String path = readableName;

        if (this instanceof MacFolder) path += ':';

        if (parent != null) path = parent.getPath() + path;
        else path = ":" + path;

        return path;
    }
}
