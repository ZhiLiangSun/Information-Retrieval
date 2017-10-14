package IRLit;

import java.io.*;

public class FileSaver {
    private File file = null;

    public FileSaver(String Path) {
        this.file = new File(Path);
    }

    public FileSaver(File file) {
        this.file = file;
    }

    public void saveContent(String content) throws IOException {
        FileWriter fwriter = new FileWriter(this.file);
        BufferedWriter bwriter = new BufferedWriter(fwriter);
        bwriter.write(content);
        bwriter.flush();

        bwriter.close();
        fwriter.close();
    }

    public void saveContent(StringBuffer content) throws IOException {
        FileWriter fwriter = new FileWriter(this.file);
        BufferedWriter bwriter = new BufferedWriter(fwriter);
        bwriter.append(content);
        bwriter.flush();

        bwriter.close();
        fwriter.close();
    }

    public void saveObject(Object obj) throws IOException {
        FileOutputStream fos = new FileOutputStream(this.file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
    }
}
