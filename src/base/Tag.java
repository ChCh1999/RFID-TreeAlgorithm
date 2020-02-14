package base;

import java.util.Random;

/**
 * Created by limingzhe in 17:02 2017/5/5.
 * Description: 标签的基础类，包含一些基本方法和属性，用于被算法的标签类继承
 *              在每一个Slot中，标签会经历发送ID、获得反馈、结束Slot三个步骤
 */
public class Tag {
    /**
     * 每个标签都有一个唯一的ID
     */
    public String ID;

    /**
     * Slot为内部类，用于模拟标签与阅读器的通信
     */
    public Slot slot;

    /**
     * Location为内部类，模拟二维坐标中标签的位置
     */
    public Location location;

    /**
     * 构造函数，可以根据传入的标签长度随机生成标签ID
     * @param tagIDLength 标签ID长度
     */
    public Tag(int tagIDLength) {
        // randomly init ID
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();

        slot = new Slot();
    }

    /**
     * 构造函数
     * @param tagIDLength 标签长度
     * @param x_range x坐标
     * @param y_range y坐标
     */
    public Tag(int tagIDLength, double x_range, double y_range) {
        this(tagIDLength);
        location = new Location(x_range, y_range);
    }

    /**
     * 标签向阅读器发送ID，将ID写入Slot类的tagID
     */
    public void transmitID() {
        slot.tagID = ID;
    }

    /**
     * 标签从Slot类的feedback获得阅读器的反馈
     * @param feedback
     */
    public void getFeedback(String feedback) {
        slot.feedback = feedback;
    }

    /**
     * 结束一个Slot，将Slot类的tagID清空
     */
    public void finishSlot() {
        slot.tagID = "";
    }

    /**
     * 模拟标签与阅读器的通信
     */
    public class Slot {
        public String tagID;
        public String feedback;

        public Slot() {
            tagID = "";
            feedback = "";
        }

        public String getSlotInfo() {
            return "tagID is " + tagID + ", feedback is " + feedback;
        }
    }

    /**
     * 模拟标签的坐标
     */
    public class Location {
        public double X_coordinate;
        public double Y_coordinate;

        public Location(double x_range, double y_range) {
            X_coordinate = new Random().nextDouble() * x_range;
            Y_coordinate = new Random().nextDouble() * y_range;
        }

        public String getLocationInfo() {
            return "X_coordinate is " + X_coordinate + ", Y_coordinate is " + Y_coordinate;
        }
    }

    public static void main(String[] args) {
        Tag t = new Tag(10, 5.8, 6.9);
        System.out.println("t.ID is " + t.ID);
        System.out.println(t.slot.getSlotInfo());
        System.out.println(t.location.getLocationInfo());
        t.transmitID();
        System.out.println(t.slot.getSlotInfo());
        t.finishSlot();
        System.out.printf(t.slot.getSlotInfo());
    }
}
