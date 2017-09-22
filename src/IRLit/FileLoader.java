package IRLit;
import java.io.*;


public class FileLoader {
    private File file = null;
    public FileLoader(String path){
        this.file = new File(path);
    }

    public FileLoader(File file){
        this.file = file;
    }

    public String loadContent() throws IOException{
        FileReader freader = new FileReader(this.file);
        BufferedReader breader = new BufferedReader(freader);
        String line = null;
        StringBuffer sb = new StringBuffer();

        while((line = breader.readLine())!= null){
            sb.append(line + "\r\n");
        }

        breader.close();
        freader.close();

        return sb.toString();
    }

    public Object loadObject() throws IOException, ClassNotFoundException{
        FileInputStream fis = new FileInputStream(this.file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object idx = ois.readObject();
        return idx;
    }


    public static void main(String args[]){

        //File f = new File("F:/IR101_JiaXiong/QueryJudgements/301/NR/CR93E-38");
        File f = new File("C:/Users/Chou01/Desktop/IR101_JiaXiong/QueryJudgements/301/NR/CR93E-38");
        FileLoader loader = new FileLoader(f);


        try {
            System.out.println(loader.loadContent());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
