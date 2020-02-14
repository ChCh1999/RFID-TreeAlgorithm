package PRB;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by limingzhe in 22:24 2017/5/9.
 * Description: 基于SRB阅读器，分为两个阶段，第一阶段使用“pair resolution”，一个slot让两个标签发送ID
 * 若发生碰撞，则两个标签必然都在；若可读，则另一个标签离开；若为空，则两个标签都丢失
 * Ref: Lai Y C, Lin C C.
 * Two blocking algorithms on adaptive binary splitting: single and pair resolutions for RFID tag identification[J].
 * IEEE/ACM Transactions on Networking (TON), 2009, 17(3): 962-975.
 */
public class Reader_PRB {
    private static Logger logger = Logger.getLogger(Reader_PRB.class);

    public double first_phase_time;
    public double second_phase_time;

    public List<String> tagIDList;
    public List<String> lastFrameTagIDList;

    public int PSC;
    public int TSC;
    public int CSC;

    public int NewCount;
    public double NewEst;
    public int TSCEXT;
    public double z;    // z is the weight and should range 0~1
    public int rRID;

    public Reader_PRB(int rRID, double z) {
        tagIDList = new ArrayList<>();
        lastFrameTagIDList = new ArrayList<>();
        PSC = -1;
        TSC = -1;
        NewCount = 0;
        NewEst = 0.0;
        TSCEXT = 0;
        this.z = z;
        this.rRID = rRID;
    }

    public void startFrame(List<Tag_PRB> tagList) {
        PSC = 0;
        if (TSC == -1) { // the reader identified no tags in the last frame
            TSC = 0;
            NewCount = 0;
        }

        NewEst = z * NewEst + (1 - z) * NewCount;
        TSCEXT = (int) (TSC + Math.ceil(0.88 * NewEst));
        NewCount = 0;
        CSC = 0;

        for (Tag_PRB t : tagList) {
            t.startFrame(TSC, TSCEXT, rRID);
        }

    }

    /**
     * 第一阶段识别标签
     */
    public String oneSlotPhase1(List<Tag_PRB> tagList) {
        String feedback;
        String tagID = "";
        int tagIDNum = 0;

        for (Tag_PRB t : tagList) {
            t.transmitIDPhase1();
            if (!t.slot.tagID.equals("")) {
                tagIDNum++;
                tagID = t.slot.tagID;
            }
        }

        if (tagIDNum == 0) {
            feedback = "idle";
            TSC -= 2;
        } else if (tagIDNum == 1) {
            feedback = "readable";
            tagIDList.add(tagID);
            PSC++;
            TSC--;
        } else {
            feedback = "collision";
            PSC += 2;
            tagIDList.add(lastFrameTagIDList.get(CSC * 2));
            tagIDList.add(lastFrameTagIDList.get(CSC * 2 + 1));
        }

        for (Tag_PRB t : tagList) {
            t.getFeedback(feedback);
            t.handleAfterSlotPhase1();
            t.finishSlot();
        }

        return feedback;
    }

    public String oneSlot(List<Tag_PRB> tagList) {
        String feedback;
        String tagID = "";
        int tagIDNum = 0;

        for (Tag_PRB t : tagList) {
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

        for (Tag_PRB t : tagList) {
            t.getFeedback(feedback);
            t.handleAfterSlot();
            t.finishSlot();
        }

        return feedback;
    }

    public int oneFrame(List<Tag_PRB> tagList) {
        logger.info("----------The frame starts----------");

        startFrame(tagList);

        int slotNum = 0;

        logger.info("-----After start-----");
        logger.info(this);
        for (Tag_PRB t : tagList)
            logger.info(t);

        logger.info("--------first phase starts--------");
        while (PSC <= (Math.ceil(TSC * 1.0 / 2) * 2 - 1)) {
            String feedback = oneSlotPhase1(tagList);
            logger.info("-----After slot-----");
            logger.info(this);
            for (Tag_PRB t : tagList)
                logger.info(t);
            logger.info("the slot is " + feedback);
            slotNum++;

            CSC++;
        }
        logger.info("--------first phase ends--------");

        TSC += Math.ceil(0.88 * NewEst);

        while (PSC <= TSC) {
            String feedback = oneSlot(tagList);
            logger.info("-----After slot-----");
            logger.info(this);
            for (Tag_PRB t : tagList)
                logger.info(t);
            logger.info("the slot is " + feedback);
            slotNum++;
        }

        lastFrameTagIDList = new ArrayList<>(tagIDList);

        logger.info("----------Whole frame has finished----------");

        return slotNum;
    }

//    public double oneFrameTime(List<Tag_PRB> tagList) {
//        // 更新时间
//        first_phase_time = 0;
//        second_phase_time = 0;
//
//        logger.info("----------The frame starts----------");
//
//        startFrame(tagList);
//
//        int slotNum = 0;
//        double time = 0.0;
//
//        logger.info("-----After start-----");
//        logger.info(this);
//        for (Tag_PRB t : tagList)
//            logger.info(t);
//
//        logger.info("--------first phase starts--------");
//        while (PSC <= (Math.ceil(TSC * 1.0 / 2) * 2 - 1)) {
//            String feedback = oneSlotPhase1(tagList);
//            logger.info("-----After slot-----");
//            logger.info(this);
//            for (Tag_PRB t : tagList)
//                logger.info(t);
//            logger.info("the slot is " + feedback);
//            slotNum++;
//            if (feedback.equals("idle")) {
//                time += Constant.T_PRB_C_and_R;
//                first_phase_time += Constant.T_PRB_C_and_R;
//            } else {
//                time += Constant.T_PRB_C_and_R;
//                first_phase_time += Constant.T_PRB_C_and_R;
//            }
//
//            CSC++;
//        }
//        logger.info("--------first phase ends--------");
//
//        TSC += Math.ceil(0.88 * NewEst);
//
//        while (PSC <= TSC) {
//            String feedback = oneSlot(tagList);
//            logger.info("-----After slot-----");
//            logger.info(this);
//            for (Tag_PRB t : tagList)
//                logger.info(t);
//            logger.info("the slot is " + feedback);
//            slotNum++;
//            if (feedback.equals("idle")) {
//                time += Constant.T_PRB_C_and_R;
//                second_phase_time += Constant.T_PRB_C_and_R;
//            } else {
//                time += Constant.T_PRB_C_and_R;
//                second_phase_time += Constant.T_PRB_C_and_R;
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
                + ", CSC = " + CSC + ", NewCount=" + NewCount + ", NewEst=" + NewEst;
    }
}
