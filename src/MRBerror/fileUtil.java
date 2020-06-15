package MRBerror;


import java.io.*;
import java.io.FileInputStream;

public class fileUtil {
    public static boolean transferData2Json(String dest) {
        BufferedOutputStream outputStream;
        BufferedInputStream inputStream;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File("log/MRBRecord.txt")));

            File target = new File(dest);
            //"res\\filetest.txt"
            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            outputStream = new BufferedOutputStream(new FileOutputStream(new File(dest)));
            outputStream.write('[');
            outputStream.write('\n');

            //略过第一个','
            inputStream.read();

            //拷贝
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            //写入]
            outputStream.write(']');

            //结束
            inputStream.close();
            outputStream.close();
            File file = new File("log/MRBRecord.log");
            boolean result = file.delete();
            if (!result) {
                System.gc();    //回收资源
                file.delete();
            }
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }


    }

    private FileOutputStream fo;
    private FileOutputStream foClear;

    public fileUtil(String filePath) {
        File target = new File(filePath);
        //"res\\filetest.txt"

        try {
            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            this.foClear = new FileOutputStream(target);
            this.fo = new FileOutputStream(target, true);

//            System.out.println("init file writer for " + target.getAbsolutePath() + "successfully");

        } catch (FileNotFoundException fnfe) {
            System.out.println("error in init file util");
        }
    }

    /**
     * 原构造函数似乎有无法最近信息的bug.
     * foclear创建FileOutputStream会导致文件中原来的信息被覆盖.
     * Feng Wenhan添加于2020.4.27
     *
     * @param filePath 目标地址
     * @param append   是否将输出追加到文件尾
     */
    public fileUtil(String filePath, boolean append) {

        File target = new File(filePath);
        //"res\\filetest.txt"

        try {
            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (!append) {
                this.foClear = new FileOutputStream(target);
            }
            this.fo = new FileOutputStream(target, true);

//            System.out.println("init file writer for " + target.getAbsolutePath() + "successfully");

        } catch (FileNotFoundException fnfe) {
            System.out.println("error in init file util");
        }
    }

    public void clearmsg() {
        try {
            this.foClear.write("".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean writemsg(String msg) {
        try {
            this.fo.write((msg).getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.fo.close();
    }

    public static void main(String[] args) {
        transferData2Json("log/c.json");
    }
}
