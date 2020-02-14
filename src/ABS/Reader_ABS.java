package ABS;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 21:05 2017/5/8.
 * Description: ABS算法的阅读器有PSC和TSC两个计数器，PSC记录了当前识别的标签数量，TSC为标签集合数-1
 * 每一个Slot后，阅读器通过碰撞情况修改PSC和TSC，TSC>PSC时识别结束
 * 每一个frame开始前，阅读器广播TSC，阅读器让ASC=0~TSC
 * Ref: Myung J, Lee W, Srivastava J, et al.
 * Tag-splitting: adaptive collision arbitration protocols for RFID tag identification[J].
 * IEEE transactions on parallel and distributed systems, 2007, 18(6): 763-775.
 */
public class Reader_ABS {
    private static Logger logger = Logger.getLogger(Reader_ABS.class);

    public List<String> tagIDList;

    public int PSC;
    public int TSC;

    public Reader_ABS() {
        tagIDList = new ArrayList<>();
        PSC = -1;
        TSC = -1;
    }

    public void startFrame(List<Tag_ABS> tagList) {
        PSC = 0;
        if (TSC == -1)  // the reader identified no tags in the last frame
            TSC = 0;
        for (Tag_ABS t : tagList)
            t.startFrame(TSC);
    }

    public String oneSlot(List<Tag_ABS> tagList) {
        String feedback;
        String tagID = "";
        int tagIDNum = 0;

        for (Tag_ABS t : tagList) {
            t.transmitID();
            if (!t.slot.tagID.equals("")) {
                tagIDNum++;
                tagID = t.slot.tagID;
            }
        }

        if (tagIDNum == 0) {
            feedback = "idle";
            TSC--;
        } else if (tagIDNum == 1) {
            feedback = "readable";
            tagIDList.add(tagID);
            PSC++;
        } else {
            feedback = "collision";
            TSC++;
        }

        for (Tag_ABS t : tagList) {
            t.getFeedback(feedback);
            t.handleAfterSlot();
            t.finishSlot();
        }

        return feedback;
    }

    public int oneFrame(List<Tag_ABS> tagList) {
        logger.info("----------The frame starts----------");

        startFrame(tagList);

        int slotNum = 0;

        logger.info("-----Before start-----");
        logger.info(this);
        for (Tag_ABS t : tagList)
            logger.info(t);

        while (PSC <= TSC) {
            String feedback = oneSlot(tagList);
            logger.info("-----After slot-----");
            logger.info(this);
            for (Tag_ABS t : tagList)
                logger.info(t);
            logger.info("the slot is " + feedback);
            slotNum++;
        }

        logger.info("----------Whole frame has finished----------");

        return slotNum;
    }


    @Override
    public String toString() {
        return "reader's PSC = " + PSC + ", TSC = " + TSC;
    }
}
