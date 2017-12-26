package Utils;

import java.io.*;

public class StreamGobbler extends Thread {
    InputStream is;
    String type;
    OutputStream os;

    public StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }

    public StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }

    public void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            if (os != null)
                pw = new PrintWriter(os);

            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (pw != null)
                    pw.println(line);

                //if (line.length() > 0)
                //    System.out.println(type + "> " + line);
            }

            if (pw != null)
                pw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                br.close();
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
