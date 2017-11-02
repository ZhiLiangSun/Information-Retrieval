package Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> getRelevantDocList(int queryNumber) {
        List<String> rLists = new ArrayList<String>();
        BufferedReader br;
        try {
            String rListPath = Path.Data_Path + "/answer/list/" + queryNumber + "RList";
            br = new BufferedReader(new FileReader(rListPath));
            String line = br.readLine();

            while (line != null) {
                rLists.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rLists;
    }

    public static List<String> getStopWords() {
        List<String> stopWords = new ArrayList<String>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Path.Stopwords_Path));
            String line = br.readLine();

            while (line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }


    // get file's attribute
    public static String getExt(String path) {
        int idx = path.lastIndexOf(".");
        if (idx < 0)
            return "";
        return path.substring(idx).toLowerCase();
    }

    public static boolean delete(File file) {
        boolean flag = true;
        if (file.isFile())
            flag &= file.delete();
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                flag &= delete(f);
            flag &= file.delete();
        }
        return flag;
    }

    public static boolean copy(String src, String dest, boolean override) {
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            System.out.println("File doesn't exist.");
            return false;
        } else if (!srcFile.isFile()) {
            System.out.println("Not a File.");
            return false;
        }
        File destFile = new File(dest);
        if (destFile.exists()) {
            if (override)
                destFile.delete();
            else {
                System.out.println("Not allowed override.");
                return false;
            }
        } else {
            if (!destFile.getParentFile().exists()) {
                if (!destFile.getParentFile().mkdirs()) {
                    System.out.println("File doesn't exist and create failed.");
                    return false;
                }
            }
        }

        int byteread = 0;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            while ((byteread = in.read(buffer)) != -1)
                out.write(buffer, 0, byteread);
            return true;
        } catch (Exception e) {
            System.out.println("Copy file failed.");
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (Exception e) {
                System.out.println("Close stream failed.");
                e.printStackTrace();
            }
        }
    }

    public static boolean copy(String src, String dest) {
        return copy(src, dest, true);
    }

    public static boolean cut(String src, String dest) {
        return new File(src).renameTo(new File(dest));
    }

    public static String readFile(String filePath, String encoding) {

        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding));
            String s = "";
            StringBuffer sb = new StringBuffer();
            while ((s = br.readLine()) != null)
                sb.append(s + "\n");
            br.close();
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Read file failed.");
            return null;
        }
    }


    public static String readFile(String filePath) {
        return readFile(filePath, "utf-8");
    }

    public static boolean writeFile(String filePath, String text, String encoding) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, encoding));
            bw.append(text);
            bw.close();
            return true;
        } catch (Exception e) {
            System.out.println("Write in Failed.");
            return false;
        }
    }


    public static boolean writeFile(String filePath, String text) {
        return writeFile(filePath, text, "utf-8");
    }

    public interface TraverseExecuter {
        public void execute(String filePath, String fileName);
    }

    public static void traverse(String filePath, FilenameFilter fileNameFilter, TraverseExecuter executer) {
        try {
            File root = new File(filePath);
            for (File file : root.listFiles(fileNameFilter)) {
                String path = file.getAbsolutePath();
                if (file.isFile())
                    executer.execute(path, file.getName());
                else if (file.isDirectory())
                    traverse(path, fileNameFilter, executer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<File> listAllFiles(String filePath, FilenameFilter fileNameFilter) {
        List<File> files = new ArrayList<File>();
        try {
            File root = new File(filePath);
            if (!root.exists()) return files;
            if (root.isFile()) files.add(root);
            else {
                for (File file : root.listFiles(fileNameFilter)) {
                    if (file.isFile()) files.add(file);
                    else if (file.isDirectory()) {
                        files.addAll(listAllFiles(file.getAbsolutePath(), fileNameFilter));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public static List<File> listAllFiles(String filePath) {
        return listAllFiles(filePath, null);
    }


    public static void traverse(String filePath, TraverseExecuter executer) {
        traverse(filePath, null, executer);
    }

    public static void deleteEmptyFolder(File file) {
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            delete(file);
            System.out.println("Files have been deleted" + file.getAbsolutePath());
        }
        for (File f : files) {
            if (f.isDirectory())
                deleteEmptyFolder(f);
        }
    }


    public static void deleteEmptyFolder(String filePath) {
        deleteEmptyFolder(new File(filePath));
    }

    public static void writeIO(InputStream is, OutputStream os, Boolean closeInput, Boolean closeOutput) throws IOException {
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = is.read(buf)) != -1)
            os.write(buf, 0, len);
        if (closeInput) {
            if (is != null)
                is.close();
        }
        if (closeOutput) {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }


    public static void writeIO(InputStream is, OutputStream os) throws IOException {
        writeIO(is, os, false, false);
    }

}


