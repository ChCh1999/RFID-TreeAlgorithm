package SRB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 20:38$ 2017/5/9.
 * Description: SRB算法的一些测试
 */
public class Test_SRB {
    public static void main(String[] args) {
        Reader_SRB r = new Reader_SRB(1, 0.5);
        List<Tag_SRB> list = new ArrayList<>();
        list.add(new Tag_SRB(10));
        list.add(new Tag_SRB(10));
        list.add(new Tag_SRB(10));
//        System.out.println(r.oneFrame(list));
//        System.out.println(r.oneFrameTime(list));
        list.add(new Tag_SRB(10));
//        System.out.println(r.oneFrame(list));
//        System.out.println(r.oneFrameTime(list));
    }
}
