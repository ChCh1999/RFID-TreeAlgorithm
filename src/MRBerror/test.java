package MRBerror;

import java.util.*;

public class test {
    public static void main(String[] args) {
//        Random r=new Random();
//        int count=0;
//        for (int i = 0; i <1000 ; i++) {
//            if (r.nextInt(100)>20 && r.nextInt(100)>10 && r.nextInt(100)>10 )count++;
//        }
//        System.out.println(count);
        MRB_Reader r = new MRB_Reader();
        r.fileWriterFeng.writemsg("111");
        r.fileWriterFeng.writemsg("222");
        r = new MRB_Reader();
        r.fileWriterFeng.writemsg("333");
    }

}
