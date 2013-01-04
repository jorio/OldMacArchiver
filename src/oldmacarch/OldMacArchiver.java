package oldmacarch;

import oldmacarch.compactpro.CptArchive;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import oldmacarch.stuffit.SitArchive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class OldMacArchiver {
    private static final boolean hostIsMacOS = System.getProperty("os.name")
            .toLowerCase().startsWith("mac");

    private static final OptionParser makeParser() {
        return new OptionParser() {{
//            accepts("c", "Create Compact Pro compatible archive with optional comment")
//                    .withOptionalArg().ofType(String.class).describedAs("comment").defaultsTo("");

            accepts("c", "Create Compact Pro-compatible archive");

            accepts("s", "Create StuffIt 1.5.1-compatible archive");

            accepts("b", "Wrap archive in MacBinary container");

            accepts("p", "Codepage (character set) to use to encode filenames")
                    .withRequiredArg().ofType(String.class).defaultsTo("MacRoman");

            accepts("l", "List available codepages").forHelp();

            accepts("f", "Output file").withRequiredArg().required();

            accepts("h", "Help").forHelp();

            accepts("d", "Default 8-byte signature to fall back on (type, creator)")
                    .withRequiredArg().ofType(String.class).defaultsTo("TEXTttxt");

            accepts("x", "Extended attributes detection method (resource fork and Finder info). " +
                    "Can be one of the following: MacOSXNative, AppleDouble, Basilisk. " +
                    "'MacOSXNative' only works on Mac OS X, but it yields the most reliable results.")
                    .withRequiredArg().ofType(String.class)
                    .defaultsTo(hostIsMacOS? "MacOSXNative": "AppleDouble");
        }};
    }

    private static Archive createArchive(OptionSet options, Charset macCharset) {
        boolean wantCpt = options.has("c");
        boolean wantSit = options.has("s");

        if (wantCpt && wantSit) {
            throw new IllegalArgumentException("Only one archive type can be specified");
        } else if (!wantCpt && !wantSit) {
            throw new IllegalArgumentException("Must specify an archive type");
        } else if (wantCpt) {
            String comment = "";//(String)options.valueOf("c");
            return new CptArchive(macCharset, comment);
        } else {
            return new SitArchive(macCharset);
        }
    }

    private static XattrDetector createDetector(String n) {
        n = n.toLowerCase();

        if (n.equals("macosxnative"))       return new oldmacarch.detector.MacOSXNative();
        else if (n.equals("appledouble"))   return new oldmacarch.detector.AppleDouble();
        else if (n.equals("basilisk"))      return new oldmacarch.detector.Basilisk();
        else throw new IllegalArgumentException("Unknown detector " + n);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("OldMacArchiver - written by Iliyas Jorio, 2013");

        Charset         macCharset;
        Archive         arc;
        File            outFile;
        XattrDetector   detector;
        OptionParser    parser      = makeParser();
        OptionSet       options     = parser.parse(args);
        String          charsetName = (String)options.valueOf("p");
        String          defaultSig  = (String)options.valueOf("d");
        String          detName     = (String)options.valueOf("x");
        boolean         wantWrapper = options.has("b");

        if (options.has("h")) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }

        if (options.has("l")) {
            Map<String, Charset> charsets = Charset.availableCharsets();
            for (String n: charsets.keySet())
                System.out.println(n);
            System.exit(0);
        }

        if (options.nonOptionArguments().isEmpty()) {
            System.out.println("WARNING: empty archive");
        }

        macCharset      = Charset.forName(charsetName);
        outFile         = new File((String)options.valueOf("f"));
        arc             = createArchive(options, macCharset);
        detector        = createDetector(detName);
        MacFinf.setDefaultSignature(defaultSig.getBytes(macCharset));

        System.out.println("========================== ARCHIVE SUMMARY ==========================");
        System.out.println("Output file...................: " + outFile);
        System.out.println("Archive type..................: " + arc.getHumanArchiveType());
        System.out.println("Wrapper.......................: " + (wantWrapper? "MacBinary I": "none"));
        System.out.println("Charset for filenames.........: " + macCharset);
        System.out.println("Ext'd attr. detection method..: " + detName);
        System.out.println("Default signature.............: " + defaultSig);
        System.out.println("=====================================================================");

        System.out.print("Preparing archive tree...");

        for (String s : options.nonOptionArguments()) {
            arc.add(new File(s), null, detector);
        }

        System.out.println("\nWriting archive...");

        if (wantWrapper) {
            String fn = outFile.getName();
            if (fn.toLowerCase().endsWith(".bin")) {
                fn = fn.substring(0, fn.length()-4);
            } else {
                System.out.println("WARNING: you should name it with .bin");
            }
            arc.writeWithMacBinary1Wrapper(outFile, fn);
        } else {
            arc.write(outFile);
        }
    }
}
