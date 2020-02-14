package OBTT;

import java.util.Random;

public class OBTT_Tag {
    public String ID;

    public int TC;

    /**
     * Slot为内部类，用于模拟标签与阅读器的通信
     */
    public Slot slot;

    public boolean isSilent;

    public OBTT_Tag(int tagIDLength) {
        TC = 0;
        isSilent = false;
        slot = new Slot();

        // randomly init ID
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();
    }

    /**
     * 开始一轮识别
     */
    public void startFrame(int m) {
        TC = new Random().nextInt(m);
    }

    /**
     * 得到反馈后进行的处理
     */
    public void handleAfterSlot() {
        if (TC == 0) {
            if (slot.feedback.equals("readable")) {
                TC--;
                isSilent = true;
            } else {
                int i = Integer.valueOf(slot.feedback);
                if (ID.charAt(i) == '1')    TC++;
            }
        } else {
            if (slot.feedback.equals("idle")) {
                TC--;
            } else if (slot.feedback.equals("readable")) {
                TC--;
            } else {
                TC++;
            }
        }
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
     * 标签向阅读器发送ID，将ID写入Slot类的tagID
     */
    public void transmitID() {
        if (TC == 0 && !isSilent)
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

    @Override
    public String toString() {
        return "OBTT_Tag{" +
                "ID='" + ID + '\'' +
                ", TC=" + TC +
                ", isSilent=" + isSilent +
                '}';
    }
}
