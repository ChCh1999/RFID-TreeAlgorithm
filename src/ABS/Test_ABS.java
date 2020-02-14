package ABS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 22:00 2017/5/8.
 * Description: ABS算法的测试
 */
public class Test_ABS {
    public static void main(String[] args) {
        Reader_ABS r = new Reader_ABS();
        List<Tag_ABS> list = new ArrayList<>();
        list.add(new Tag_ABS(10));
        list.add(new Tag_ABS(10));
        list.add(new Tag_ABS(10));

//        int slotNum = r.oneFrame(list);
//        System.out.println("total slotNum is " + slotNum);

        int slotTime = r.oneFrame(list);
        System.out.println("total slotTime is " + slotTime);

        list.add(new Tag_ABS(10));

        slotTime = r.oneFrame(list);
        System.out.println("total slotTime is " + slotTime);
    }
}
