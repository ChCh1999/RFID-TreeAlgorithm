package base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 18:24 2017/5/5.
 * Description: 阅读器的基础类，可以和标签类进行交互
 */
public class Reader {

    /**
     * 阅读器已经识别到的标签集合
     */
    public List<String> tagIDList;

    /**
     * 构造函数
     */
    public Reader() {
        tagIDList = new ArrayList<>();
    }

    /**
     * 阅读器进行一个Slot的完整过程
     * @param tagList 阅读器范围内的所有标签
     * @return 该Slot的碰撞情况
     */
    public String oneSlot(List<Tag> tagList) {
        String feedback;
        String tagID = "";
        int tagIDNum = 0;

        for (Tag t : tagList) {
            t.transmitID();
            if (!t.slot.tagID.equals("")) {
                tagIDNum++;
                tagID = t.slot.tagID;
            }
        }

        if (tagIDNum == 0)
            feedback = "idle";
        else if (tagIDNum == 1) {
            feedback = "readable";
            tagIDList.add(tagID);
        } else {
            feedback = "collision";
        }


        for (Tag t : tagList) {
            t.getFeedback(feedback);
            t.finishSlot();
        }
        return feedback;
    }

    /**
     * 阅读器进行一轮识别（frame）的完整过程
     * @param tagList 阅读器范围内的所有标签
     */
    public void oneFrame(List<Tag> tagList) {
        String feedback = oneSlot(tagList);
        System.out.println(feedback);
        System.out.println(tagIDList.get(0));
    }

    public static void main(String[] args) {
        Reader r = new Reader();
        Tag a = new Tag(10);
        Tag b = new Tag(10);
        List<Tag> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        r.oneFrame(list);
    }
}
