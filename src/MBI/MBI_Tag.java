package MBI;
import base.Tag;

import java.util.List;
import java.util.Random;

public class MBI_Tag {
    public String ID;
    public Slot slot;
    public Location location;

    /**
     * 构造函数，可以根据传入的标签长度随机生成标签ID
     * @param tagIDLength 标签ID长度
     */
    public MBI_Tag(int tagIDLength) {
        // randomly init ID
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();

        slot = new MBI_Tag.Slot();
    }

    /**
     * 构造函数
     * @param tagIDLength 标签长度
     * @param x_range x坐标
     * @param y_range y坐标
     */
    public MBI_Tag(int tagIDLength, double x_range, double y_range) {
        this(tagIDLength);
        location = new MBI_Tag.Location(x_range, y_range);
    }

    // CS-Memory，用于存储公共字符串。不初始化，直接调用Main函数中的实体
    public List<String> CS_Memory;
    public int L;

    public void transmitID(){
        if(this.slot.feedback.startsWith("0")){
            if(this.ID.startsWith(this.slot.feedback.substring(1))){
                slot.tagID = ID.substring(slot.feedback.length()-1);
            }
        }else if(this.slot.feedback.startsWith("1")){
            if(this.ID.startsWith(this.slot.feedback.substring(1))){
                String s1 = ID.substring(slot.feedback.length()-1);
                if(s1.length() >= L){
                    String C = s1.substring(0,L);
                    String fin = "";
                    for(String s : CS_Memory){            //  对于CS中的每一个公共字符串
                        int num = 0;     // 用于确认当只有一个相同的位的时候，该位的位置（num-1）
                        int num1 = 0;    // 用于判断相同的位的个数
                        StringBuilder stri = new StringBuilder();
                        for(int i = 0;i<L;i++)
                            stri.append(0);
                        for(int i = 1;i<L+1;i++){
                            if(s.charAt(i-1) != C.charAt(i-1)){
                                num = i;
                                num1++;
                            }
                        }
                        if(num1==1){
                            fin  =  fin + stri.toString().substring(0,num-1) + "1" + stri.toString().substring(num);
                        }else{
                            fin = fin + stri.toString();
                        }
                    }
                    slot.tagID = fin;
                }
            }
        }
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

    public String toString(){
        return "the Tag ID is " + ID;
    }
}
