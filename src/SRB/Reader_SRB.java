package SRB;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 19:29$ 2017/5/9.
 * Description: SRB的阅读器在ABS的基础上增加了记录上一轮识别出的标签的list和预测新来标签的算法，公式为：
 * NewEst = z * NewEst + (1 - z) * NewCount;
 * 其中，NewEst为这一轮预测的新来标签，NewCount为上一轮的新来标签，z为权值。
 * 每轮识别开始时，广播TSC、TSCEXT、rRID。
 * 标签的tRID和rRID相等的话，该标签为原有标签，ASC不变；不等的话，该标签为新来标签，ASC=random(TSC+1~TSCEXT)
 * 然后，使用ABS算法识别
 * Ref: Lai Y C, Lin C C.
 * Two blocking algorithms on adaptive binary splitting: single and pair resolutions for RFID tag identification[J].
 * IEEE/ACM Transactions on Networking (TON), 2009, 17(3): 962-975.
 */
public class Reader_SRB {
    private static Logger logger = Logger.getLogger(Reader_SRB.class);

    public List<String> tagIDList;

    public double first_phase_time;
    public double second_phase_time;

    /**
     * 上一轮识别出的标签
     */
    public List<String> lastFrameTagIDList;

    public int PSC;
    public int TSC;

    /**
     * 新来标签的数量
     */
    public int NewCount;

    /**
     * 预测新来标签的数量
     */
    public double NewEst;

    public int TSCEXT;

    /**
     * 预测公式中的权值
     */
    public double z;    // z is the weight and should range 0~1

    /**
     * 标签ID
     */
    public int rRID;

    public Reader_SRB(int rRID, double z) {
        tagIDList = new ArrayList<>();
        lastFrameTagIDList = new ArrayList<>();
        PSC = -1;
        TSC = -1;
        NewCount = 0;
        NewEst = 0.0;
        TSCEXT = 0;
        this.z = z;
        this.rRID = rRID;
        first_phase_time = 0;
        second_phase_time = 0;
    }

    public void startFrame(List<Tag_SRB> tagList) {
        tagIDList.clear();
        PSC = 0;
        if (TSC == -1) { // the reader identified no tags in the last frame
            TSC = 0;
            NewCount = 0;
        }

        NewEst = z * NewEst + (1 - z) * NewCount;
        TSCEXT = (int) (TSC + Math.ceil(0.88 * NewEst));
        NewCount = 0;

        for (Tag_SRB t : tagList) {
            t.startFrame(TSC, TSCEXT, rRID);
        }

        TSC = TSCEXT;
    }

    public String oneSlot(List<Tag_SRB> tagList) {
        String feedback;
        String tagID = "";
        int tagIDNum = 0;

        for (Tag_SRB t : tagList) {
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
            if (!lastFrameTagIDList.contains(tagID))
                NewCount++;
            tagIDList.add(tagID);
            PSC++;
        } else {
            feedback = "collision";
            TSC++;
        }

        for (Tag_SRB t : tagList) {
            t.getFeedback(feedback);
            t.handleAfterSlot();
            t.finishSlot();
        }

        return feedback;
    }

    public int oneFrame(List<Tag_SRB> tagList) {
        logger.info("----------The frame starts----------");

        startFrame(tagList);

        int slotNum = 0;

        logger.info("-----After start-----");
        logger.info(this);
        for (Tag_SRB t : tagList)
            logger.info(t);

        while (PSC <= TSC) {
            String feedback = oneSlot(tagList);
            logger.info("-----After slot-----");
            logger.info(this);
            for (Tag_SRB t : tagList)
                logger.info(t);
            logger.info("the slot is " + feedback);
            slotNum++;
        }

        lastFrameTagIDList = new ArrayList<>(tagIDList);

        logger.info("----------Whole frame has finished----------");

        return slotNum;
    }

//    public double oneFrameTime(List<Tag_SRB> tagList) {
//
//        logger.info("----------The frame starts----------");
//
//        startFrame(tagList);
//
//        int slotNum = 0;
//        double time = 0;
//
//        logger.info("-----After start-----");
//        logger.info(this);
//        for (Tag_SRB t : tagList)
//            logger.info(t);
//
//        while (PSC <= TSC) {
//            String feedback = oneSlot(tagList);
//            logger.info("-----After slot-----");
//            logger.info(this);
//            for (Tag_SRB t : tagList)
//                logger.info(t);
//            logger.info("the slot is " + feedback);
//            slotNum++;
//            if (feedback.equals("idle")) {
//                time += Constant.T_I;
//            } else {
//                time += Constant.T_ABS_C_and_R;
//            }
//        }
//
//        lastFrameTagIDList = new ArrayList<>(tagIDList);
//
//        logger.info("----------Whole frame has finished----------");
//
//        return time;
//    }

//    public double oneFrameTimeSeprate(List<Tag_SRB> tagList, double Rs) {
//        // 更新时间
//        first_phase_time = 0;
//        second_phase_time = 0;
//
//        logger.info("----------The frame starts----------");
//
//        startFrame(tagList);
//
//        int slotNum = 0;
//        double time = 0;
//
//        logger.info("-----After start-----");
//        logger.info(this);
//        for (Tag_SRB t : tagList)
//            logger.info(t);
//
//        while (PSC <= TSC) {
//            String feedback = oneSlot(tagList);
//            logger.info("-----After slot-----");
//            logger.info(this);
//            for (Tag_SRB t : tagList)
//                logger.info(t);
//            logger.info("the slot is " + feedback);
//            slotNum++;
//            if (feedback.equals("idle")) {
//                time += Constant.T_I;
//                if (tagIDList.size() <= Rs * lastFrameTagIDList.size())
//                    first_phase_time += Constant.T_I;
//                else
//                    second_phase_time += Constant.T_I;
//            } else {
//                time += Constant.T_ABS_C_and_R;
//                if (tagIDList.size() <= Rs * lastFrameTagIDList.size())
//                    first_phase_time += Constant.T_ABS_C_and_R;
//                else
//                    second_phase_time += Constant.T_ABS_C_and_R;
//            }
//        }
//
//        lastFrameTagIDList = new ArrayList<>(tagIDList);
//
//        logger.info("----------Whole frame has finished----------");
//
//        return time;
//    }

    @Override
    public String toString() {
        return "Reader_QT's TSC=" + TSC + ", PSC=" + PSC + ", TSCEXT=" + TSCEXT
                + ", NewCount=" + NewCount + ", NewEst=" + NewEst;
    }
}
