package org.neo4j.gis.spatial.osm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class CountedFileReader extends InputStreamReader {

    private long length    = 0;
    private long charsRead = 0;

    public CountedFileReader(String path, Charset charset) throws FileNotFoundException {
        super(new FileInputStream(path), charset);
        this.length = (new File(path)).length();
    }

    public CountedFileReader(File file, Charset charset) throws FileNotFoundException {
        super(new FileInputStream(file), charset);
        this.length = file.length();
    }

    public long getCharsRead() {
        return charsRead;
    }

    public long getlength() {
        return length;
    }

    public double getProgress() {
        return length > 0 ? (double) charsRead / (double) length : 0;
    }

    public int getPercentRead() {
        return (int) (100.0 * getProgress());
    }

    public int read(char[] cbuf, int offset, int length) throws IOException {
        int read = super.read(cbuf, offset, length);
        if (read > 0)
            charsRead += read;
        return read;
    }
}
