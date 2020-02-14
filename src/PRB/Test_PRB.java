package PRB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 22:53 2017/5/9.
 * Description: PRB算法的一些测试
 */
public class Test_PRB {
    public static void main(String[] args) {
        Reader_PRB r = new Reader_PRB(1, 0.5);
        List<Tag_PRB> list = new ArrayList<>();
        list.add(new Tag_PRB(10));
        list.add(new Tag_PRB(10));
        list.add(new Tag_PRB(10));
        System.out.println(r.oneFrame(list));

        list.add(new Tag_PRB(10));
//        System.out.println(r.oneFrame(list));
//        System.out.println(r.oneFrameTime(list));
    }
}
