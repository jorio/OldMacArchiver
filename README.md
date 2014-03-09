# OldMacArchiver

## A file archiver for vintage Mac archive formats (CompactPro & StuffIt 1.5)

If you need to retrieve archived files from an old 68K Mac from the late 80's
or early 90's, chances are the excellent [Unarchiver](http://unarchiver.c3.cx/)
can extract them. But if you need to move files *to* an old Mac from a modern
computer, you probably need to package them in a suitable format first.

**OldMacArchiver** is a cross-platform utility program that lets you package
files in the **StuffIt 1.5** and **CompactPro** formats, which were fairly
widespread on 68K Macs. It can handle resource forks even on non-HFS
filesystems -- in fact, my main OS during development was GNU/Linux.

This is not meant to be a serious project, it was just fun to poke around
obsolete file formats and learn about the innards of ancient systems.


## Features

- Cross-platform (Java). Should also work on PPC Macs with OS X 10.4

- Output formats (fairly widespread on vintage Macs):

    - **CompactPro** (".cpt")

    - **StuffIt 1.5** (".sit")

- Optionally wraps the archive in a **MacBinary I** container (".bin") to avoid
  fiddling with type/creator codes on the Mac side

- Handles resource forks, even on platforms lacking them when a substitute is
  available:

    - AppleDouble files

    - .finf/.rsrc (created by BasiliskII)

    - Native HFS+ resource forks (OS X only)

- Attempts to preserve type/creator codes and modification times


## Limitations

- Filenames can't exceed 31 bytes nor contain slashes due to limitations of the
  original HFS filesystem

- No compression (on purpose, please read on)


## Why is there no compression?

Philosophically, OldMacArchiver is an equivalent of **tar** that keeps
compatibility with old Macs in mind. It pretty much just stores files -- along
with some metadata -- without compressing them.

Compression is left out on purpose: my goal was to archive a bunch of
seldom-used old Mac files with a modern and efficient external compression
scheme, while retaining the ability to transfer them to an old Mac eventually.
I archive my files using this utility, then compress them with `xz` to enjoy a
high compression rate; if I ever need to use them on an old Mac, I can just
`unxz` them beforehand.

Therefore, if you want compression, use the compression program of your choice
on your modern OS and decompress the files before sending them to an old Mac.
If you want to send compressed data *directly* to an old Mac, I'm afraid this
isn't the right tool for the job.


## How to use

Build:

    gradle build

You can now use the JAR file located in build/libs. Move it to some place
convenient for you to work with.

Get help:

    java -jar OldMacArchiver.jar -h

### Examples

Package directory `mydir` as a CompactPro archive:

    java -jar OldMacArchiver.jar -c mydir -f mydir.cpt

Package directory `mydir` as a StuffIt 1.5 archive:

    java -jar OldMacArchiver.jar -s mydir -f mydir.sit

Package directory `mydir` as a StuffIt 1.5 archive wrapped in a MacBinary container:

    java -jar OldMacArchiver.jar -s -b mydir -f mydir.sit

Package some text files as "read-only" SimpleText files (type `ttro`, creator
`ttxt`) in a CompactPro archive:

    java -jar OldMacArchiver.jar -c -d ttrottxt text1.txt text2.txt -f text.cpt


## References

http://code.google.com/p/theunarchiver/wiki/CompactProSpecs

http://code.google.com/p/theunarchiver/wiki/StuffItFormat

http://web.archive.org/web/20110719072636/http://users.phg-online.de/tk/netatalk/doc/Apple/v2/AppleSingle_AppleDouble.pdf

http://files.stairways.com/other/macbinaryii-standard-info.txt

