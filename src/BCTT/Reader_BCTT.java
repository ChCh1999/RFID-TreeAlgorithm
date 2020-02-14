package BCTT;

import base.Tag;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Reader_BCTT {
    private static Logger logger = Logger.getLogger(Reader_BCTT.class);
    public double first_phase_time;
    public double second_phase_time;
    /**
     * 识别的标签集合
     */
    public List<String> tagIDList;

    /**
     * 上一轮识别的标签集合
     */
    public List<String> lastFrameTagIDList;

    /**
     * 前缀队列
     */
    public Queue<String> prefixQueue = new LinkedList<>();


    public Reader_BCTT() {
        tagIDList = new ArrayList<>();
        lastFrameTagIDList = new ArrayList<>();
    }

    public String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        for (int i = 0; i < strs[0].length(); i++) {
            for (int j = 1; j < strs.length; j++) {
                if (strs[j].length() < i + 1 || strs[j].charAt(i) != strs[0].charAt(i)) {
                    return strs[0].substring(0, i);
                }
            }
        }
        return strs[0];
    }

    public void oneSlot(List<Tag> tagList) {

        String prefix = prefixQueue.poll();
        List<String> respondIDList = new ArrayList<>();

        logger.info("-----时隙开始");
        logger.info("前缀" + prefix);

        if (prefix.equals("all")) {
            for (Tag t : tagList) {
                respondIDList.add(t.ID);
            }
        } else {
            for (Tag t : tagList) {
                if (t.ID.substring(0, prefix.length()).equals(prefix))
                    respondIDList.add(t.ID);
            }
        }

        if (respondIDList.size() == 1) {
            tagIDList.add(respondIDList.get(0));
            logger.info(respondIDList.get(0) + "被识别");
        } else if (respondIDList.size() == 0) {
            logger.info("该时隙为空");
        } else {
            String longestCommonPrefix = longestCommonPrefix(respondIDList.toArray(new String[respondIDList.size()]));
            logger.info("回复标签：");
            for (String s : respondIDList)
                logger.info(s);
            logger.info("有多个标签回复，最长子前缀是：" + longestCommonPrefix);
            prefixQueue.offer(longestCommonPrefix + "0");
            prefixQueue.offer(longestCommonPrefix + "1");
        }

    }

    public int oneFrame(List<Tag> tagList) {


        logger.info("----------The frame starts----------");

        prefixQueue.offer("all");

        int slotNum = 0;

        for (String s : lastFrameTagIDList) {
            for (Tag t : tagList) {
                if (s.equals(t.ID)) {
                    tagIDList.add(s);
                }
            }
            slotNum++;
        }

        List<Tag> arrivingTagList = new ArrayList<>();
        for (Tag t : tagList) {
            if (!tagIDList.contains(t.ID)) {
                arrivingTagList.add(t);
            }
        }

        logger.info("刚来的标签：");
        for (Tag t : arrivingTagList
                ) {
            logger.info(t.ID);
        }

        while (!prefixQueue.isEmpty()) {
            oneSlot(arrivingTagList);
            slotNum++;
        }

        logger.info("识别到的的标签：");
        for (String s : this.tagIDList
                ) {
            logger.info(s);
        }

        logger.info("----------Whole frame has finished, the total slots are " + slotNum + "----------");

        prefixQueue.clear();
        lastFrameTagIDList = new ArrayList<>(tagIDList);
        tagIDList.clear();

        return slotNum;
    }

//    public double oneFrameTime(List<Tag> tagList) {
//        // 更新时间
//        first_phase_time = 0;
//        second_phase_time = 0;
//
//        logger.info("----------The frame starts----------");
//
//        prefixQueue.offer("all");
//
//        double slotTime = 0.0;
//
//        for (String s: lastFrameTagIDList) {
//            boolean isExist = false;
//            for (Tag t : tagList){
//                if (s.equals(t.ID)) {
//                    tagIDList.add(s);
//                    isExist = true;
//                    slotTime += Constant.T_BCTT_R;
//                    first_phase_time += Constant.T_BCTT_R;
//                }
//            }
//            if (!isExist) {
//                slotTime += Constant.T_BCTT_I;
//                first_phase_time += Constant.T_BCTT_I;
//            }
//        }
//
//        List<Tag> arrivingTagList = new ArrayList<>();
//        for (Tag t : tagList) {
//            if (!tagIDList.contains(t.ID)) {
//                arrivingTagList.add(t);
//            }
//        }
//
//        logger.info("刚来的标签：");
//        for (Tag t: arrivingTagList
//                ) {
//            logger.info(t.ID);
//        }
//
//        while (!prefixQueue.isEmpty()) {
//            oneSlot(arrivingTagList);
//            slotTime += Constant.T_BCTT_Second;
//            second_phase_time += Constant.T_BCTT_Second;
//        }
//
//        logger.info("识别到的的标签：");
//        for (String s: this.tagIDList
//                ) {
//            logger.info(s);
//        }
//
//        logger.info("----------Whole frame has finished " + "----------");
//
//        prefixQueue.clear();
//        lastFrameTagIDList = new ArrayList<>(tagIDList);
//        tagIDList.clear();
//
//        return slotTime;
//    }
}
