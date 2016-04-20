package com.looseboxes.idisc.common.io;

import com.looseboxes.idisc.common.util.Logx;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class StreamReader {
    public String readContents(InputStream is) throws IOException {

        InputStreamReader isr = null;

        BufferedReader br = null;

        try{

// This seemed to cause error
//Util.debug(null, this.getClass(), "Reading from input stream");

            isr = new InputStreamReader(is, Charset.forName("UTF-8"));

            br = new BufferedReader(isr);

            String jsonText = readAll(br);

// This seemed to cause error
//Util.debug(null, this.getClass(), "Read: "+(jsonText==null?"null":jsonText.length())+" chars");

            return jsonText;

        }finally{

            close(br);

            close(isr);
        }
    }

    private String readAll(BufferedReader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            String cp = rd.readLine();
            if (cp == null) {
                return sb.toString();
            }
            sb.append(cp);
        }
    }

    public void close(Closeable cl) {
        if (cl != null) {
            try {
                cl.close();
            } catch (Exception e) {
                Logx.debug(getClass(), "Error closing stream: " + e);
            }
        }
    }
}
