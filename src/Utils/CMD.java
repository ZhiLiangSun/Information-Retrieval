package Utils;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class CMD {
    /**
     * run evaluate bat
     *
     * @param methodName
     */
    public static void run(String methodName) {
        Runtime r = Runtime.getRuntime();
        Process p;
        try {
            String path = "C:/Users/Lab714/Desktop/Information-Retrieval/eval.bat";
            p = r.exec("cmd.exe /c  " + path);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            outGobbler.start();
            p.waitFor();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            e.printStackTrace();
        }

        try {
            // rename the file
            File oldFile = new File("C:/Users/Lab714/Desktop/Exp_output/Exp_output");
            File newFile = new File(oldFile.getParent(), methodName);
            Files.move(oldFile, newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
