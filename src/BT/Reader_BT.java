package BT;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 20:49 2017/5/5.
 * Description: 阅读器含有counter，初始化为0，发送碰撞counter+1，否则counter-1，当counter<0时该轮识别结束
 * Ref: Myung J, Lee W, Srivastava J, et al.
 * Tag-splitting: adaptive collision arbitration protocols for RFID tag identification[J].
 * IEEE transactions on parallel and distributed systems, 2007, 18(6): 763-775.
 */
public class Reader_BT {
    private static Logger logger = Logger.getLogger(Reader_BT.class);

    /**
     * 识别的标签集合
     */
    public List<String> tagIDList;

    /**
     * 记录了目前的标签集合数-1
     */
    public int counter;

    public Reader_BT() {
        tagIDList = new ArrayList<>();
        counter = 0;
    }

    public void startFrame(List<Tag_BT> tagList) {
        this.counter = 0;

        for (Tag_BT t : tagList) {
            t.startFrame();
        }
    }

    public String oneSlot(List<Tag_BT> tagList) {
        String feedback;
        String tagID = "";
        int tagIDNum = 0;

        for (Tag_BT t : tagList) {
            t.transmitID();
            if (!t.slot.tagID.equals("")) {
                tagIDNum++;
                tagID = t.slot.tagID;
            }
        }

        if (tagIDNum == 0) {
            feedback = "idle";
            counter--;
        } else if (tagIDNum == 1) {
            feedback = "readable";
            tagIDList.add(tagID);
            counter--;
        } else {
            feedback = "collision";
            counter++;
        }

        for (Tag_BT t : tagList) {
            t.getFeedback(feedback);
            t.handleAfterSlot();
            t.finishSlot();
        }

        return feedback;
    }

    public int oneFrame(List<Tag_BT> tagList) {
        logger.info("----------The frame starts----------");

        startFrame(tagList);

        int slotNum = 0;

        logger.info("-----Before frame-----");
        logger.info(this);
        for (Tag_BT t : tagList)
            logger.info(t);

        while (counter >= 0) {
            String feedback = oneSlot(tagList);
            logger.info("-----After slot-----");
            logger.info(this);
            for (Tag_BT t : tagList)
                logger.info(t);
            logger.info("the slot is " + feedback);
            slotNum++;
        }

        logger.info("----------Whole frame has finished, the total slots are " + slotNum + "----------");
        return slotNum;
    }


    @Override
    public String toString() {
        return "reader's counter = " + counter;
    }
}
