package MRBerror;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class test {
    public static void main(String[] args) {
        List<Integer> silentedIndex = new ArrayList<Integer>();
        Random random = new Random();
        for (int i=0;i <5; i++) {
            //随机生成沉默序号
            int index =-1;
            do {
                index = random.nextInt(10);
            }while(silentedIndex.contains(index));
            silentedIndex.add(index);
        }
        System.out.println(silentedIndex);

    }

}
