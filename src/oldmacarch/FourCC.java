package oldmacarch;

public class FourCC {
    public final int value;

    public FourCC(String fourcc) {
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val <<= 8;
            val |= fourcc.charAt(i);
        }

        value = val;
    }

    public FourCC(byte[] array, int offset) {
        int val = 0;
        for (int i = offset; i < offset+4; i++) {
            val <<= 8;
            val |= array[i];
        }
        value = val;
    }

    public FourCC(int val) {
        value = val;
    }
}
