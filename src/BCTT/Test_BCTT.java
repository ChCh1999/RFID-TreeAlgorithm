package BCTT;

import base.Tag;

import java.util.ArrayList;
import java.util.List;

public class Test_BCTT {
    public static void main(String[] args) {
        Reader_BCTT r = new Reader_BCTT();
        List<Tag> list1 = new ArrayList<>();

        list1.add(new Tag(10));
        list1.add(new Tag(10));
        list1.add(new Tag(10));

        int num = r.oneFrame(list1);
        list1.remove(list1.get(0));
        list1.remove(list1.get(0));
        list1.add(new Tag(10));
        list1.add(new Tag(10));
        list1.add(new Tag(10));

        num += r.oneFrame(list1);
        System.out.println(num);
    }
}
