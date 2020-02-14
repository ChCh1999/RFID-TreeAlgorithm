package OBTT;


import org.apache.log4j.Logger;
import utils.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OBTT_Reader {
    private static Logger logger = Logger.getLogger(OBTT_Reader.class);

    public int RC;

    public static void main(String[] args) {
        new OBTT_Reader().oneFrame(new OBTT_Input(4, 4));
    }

    public OBTT_Output oneFrame(OBTT_Input input) {
        logger.info("----------一帧开始----------");
        int slotNum = 0;
        double time = 0.0;

        logger.info("-----开始估计标签数-----");

        int tagNum = input.tagNum;
        int tagIDLength = input.tagIDLength;
        List<OBTT_Tag> tagList = generateTags(tagNum, tagIDLength);

        RC = 0;
        int l = tagIDLength;

        int nSB, nNB; // nSB是有标签回复的位，nNB是没有标签回复的位
        while (true) {
            logger.info("标签选择回复的比特长度：" + l);
            nSB = 0;
            nNB = 0;
            boolean[] signal = new boolean[l];
            for (int i = 0; i < tagNum; i++) {
                int rand = new Random().nextInt(l);
                signal[rand] = true;
                logger.info("标签" + i + "在第" + rand + "比特回复");
            }
            for (boolean bit : signal) {
                if (bit) nSB++;
                else nNB++;
            }
            logger.info("有标签回复的位数" + nSB);
            logger.info("没有标签回复的位数" + nNB);

            slotNum += l / tagIDLength;
            time += Constant.OneBitTime * ((int) Math.ceil(Math.log(l) / Math.log(2)) + l);

            if (nSB == l) {
                l *= 2;
            } else {
                break;
            }
        }

        int nHat = (int) Math.ceil(l * Math.log((double) l / nNB));
        logger.info("估计的标签数：" + nHat);

        int m = (int) Math.ceil(0.595824 * nHat);
        logger.info("预留的时隙数：" + m);

        startFrame(tagList, m);

        logger.info("-----发送开始命令之后-----");
        logger.info(this);
        for (OBTT_Tag t : tagList)
            logger.info(t);

        while (RC >= 0) {
            String feedback = oneSlot(tagList);
            logger.info("-----一轮" + feedback + "时隙之后-----");
            logger.info(this);
            for (OBTT_Tag t : tagList)
                logger.info(t);

            slotNum++;
            if (feedback.equals("idle")) {
                time += Constant.OneBitTime * (3 + 6);
            } else if (feedback.equals("readable")) {
                time += Constant.OneBitTime * (3 + tagIDLength);
            } else {
                time += Constant.OneBitTime * (3 + tagIDLength + (int) Math.ceil(Math.log(tagIDLength) / Math.log(2)));
            }
        }

        logger.info("总时隙数" + slotNum);
        logger.info("总时间" + time);

        return new OBTT_Output(slotNum, time);
    }

    public String oneSlot(List<OBTT_Tag> tagList) {
        String feedback;
        List<String> tagIDList = new ArrayList<>();

        for (OBTT_Tag t : tagList) {
            t.transmitID();
            if (!t.slot.tagID.equals("")) {
                tagIDList.add(t.ID);
            }
        }

        if (tagIDList.size() == 0) {
            feedback = "idle";
            RC--;
        } else if (tagIDList.size() == 1) {
            feedback = "readable";
            letSilent(tagIDList.get(0), tagList);
            RC--;
        } else {
            String s = collide(tagIDList);
            logger.info("发生碰撞，碰撞结果为" + s);
            int i = s.indexOf('x');
            int j = s.lastIndexOf('x');
            if (i == j) {
                letSilent(tagIDList.get(0), tagList);
                letSilent(tagIDList.get(1), tagList);
                feedback = "readable";
                RC--;
            } else {
                feedback = String.valueOf(i);
                RC++;
            }

        }

        for (OBTT_Tag t : tagList) {
            t.getFeedback(feedback);
            t.handleAfterSlot();
            t.finishSlot();
        }

        return feedback;
    }

    private List<OBTT_Tag> generateTags(int tagNum, int tagIDLength) {
        List<OBTT_Tag> list = new ArrayList<>();
        while (list.size() < tagNum) {
            OBTT_Tag tag = new OBTT_Tag(tagIDLength);
            boolean hasRepeat = false;
            for (OBTT_Tag t : list) {
                if (t.ID.equals(tag.ID)) {
                    hasRepeat = true;
                    break;
                }
            }
            if (!hasRepeat) {
                list.add(tag);
            }
        }
        return list;
    }

    /*
    开始一帧的初始化
     */
    public void startFrame(List<OBTT_Tag> tagList, int m) {
        this.RC = m - 1;

        for (OBTT_Tag t : tagList) {
            t.startFrame(m);
        }
    }

    /*
    该标签已经被识别，令其沉默
     */
    public void letSilent(String tagID, List<OBTT_Tag> tagList) {
        for (OBTT_Tag t : tagList) {
            if (t.ID.equals(tagID)) {
                t.isSilent = true;
            }
        }
    }

    /*
    两个标签碰撞
     */
    public String collide(List<String> tagIDList) {
        String a = tagIDList.get(0);
        for (String b : tagIDList) {
            char[] c = new char[a.length()];
            for (int i = 0; i < a.length(); i++) {
                if (a.charAt(i) == '0' && b.charAt(i) == '0') c[i] = '0';
                else if (a.charAt(i) == '1' && b.charAt(i) == '1') c[i] = '1';
                else c[i] = 'x';
            }
            a = new String(c);
        }
        return a;
    }

    @Override
    public String toString() {
        return "读写器的RC=" + RC;
    }
}
