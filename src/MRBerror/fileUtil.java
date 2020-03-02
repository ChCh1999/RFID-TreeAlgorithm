package MRBerror;


import java.io.*;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.channels.FileChannel;

public class fileUtil {
    public static boolean transferData2Json(String dest) {
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File("log/MRBRecord.log")));
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
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }


    }

    public static void main(String[] args) {
        transferData2Json("log/c.json");
    }
}
