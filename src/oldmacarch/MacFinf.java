package oldmacarch;

import java.io.*;

public class MacFinf {
    private static final FourCC zero4CC = new FourCC(0);
    private static FourCC defaultFileType = new FourCC("TEXT");
    private static FourCC defaultFileCreator = new FourCC("ttxt");
    private static final short defaultFlags = 0;

    public final FourCC type;
    public final FourCC creator;
    public final short flags;

    public static void setDefaultSignature(byte[] signature) {
        if (signature.length != 8) {
            throw new IllegalArgumentException("signature must be exactly eight bytes long");
        }
        defaultFileType = new FourCC(signature, 0);
        defaultFileCreator = new FourCC(signature, 4);
    }

    public MacFinf(boolean isFile) {
        type = isFile? defaultFileType: zero4CC;
        creator = isFile? defaultFileCreator: zero4CC;
        flags = defaultFlags;
    }

    public MacFinf(boolean isFile, InputStream in) throws IOException {
        DataInputStream r = new DataInputStream(in);

        if (isFile) {
            type = new FourCC(r.readInt());
            creator = new FourCC(r.readInt());
        } else {
            type = zero4CC;
            creator = zero4CC;
            r.readLong(); // skip type and creator
        }

        flags = r.readShort();
        r.close();
    }

    public String toString() {
        return String.format("%08x:%08x:%04x", type.value, creator.value, flags);
    }
}
