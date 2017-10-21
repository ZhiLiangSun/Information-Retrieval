package Utils;

import java.io.*;


public class FileLoader {
    private File file = null;

    public FileLoader(String path) {
        this.file = new File(path);
    }

    public FileLoader(File file) {
        this.file = file;
    }

    public String loadContent() throws IOException {
        FileReader freader = new FileReader(this.file);
        BufferedReader breader = new BufferedReader(freader);
        String line = null;
        StringBuffer sb = new StringBuffer();

        while ((line = breader.readLine()) != null) {
            sb.append(line + "\r\n");
        }

        breader.close();
        freader.close();

        return sb.toString();
    }

    public Object loadObject() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(this.file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object idx = ois.readObject();
        return idx;
    }

}
