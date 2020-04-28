package MRBerror;

import java.util.ArrayList;
import java.util.List;
public class MRB_Main {
    /**
     * 标签id长度
     */
    static int tagIDlength = 10;
    /**
     * 标签数
     */
    static int tagCount = 1000;

    /**
     * 静态错误发生概率(百分制,下同)
     */
    static int staticError = 20;
    /**
     * 动态错误中从读取器到标签信息丢失概率
     */
    static int rError = 10;
    /**
     * 动态错误中从标签到读取器信息丢失概率
     */
    static int tError = 10;
    /**
     * 多标签响应时捕获效应发生概率
     */
    static int captureError = 0;
    /**
     * Fast-Capture-Recapture标签复用率
     */
    static int thev = 80;
    /**
     * 执行模拟的轮次
     */
    static int roundCount = 1000;
    /**
     * pm收敛的阈值
     */
    static double thresholdPM = 0.000;

    public static void main(String[] args) {


        System.out.println("exactly: " + getExactlyErrorProbablity());
        int tag = 0;
        while (true) {
            System.out.println("round"+tag);
            getData(String.valueOf(tag));
            tag++;
        }
    }

    /**
     * 获取一组模拟数据
     * @param tag：添加在日志文件尾部的标签
     */
    static void getData(String tag) {
        MRB_Reader.logger.info("随机沉默");
        mutilSessionTest(0, tag);

        MRB_Reader.logger.info("唯一碰撞集沉默");
        mutilSessionTest(1, tag);

        MRB_Reader.logger.info("最小唯一碰撞集沉默");
        mutilSessionTest(2, tag);

        MRB_Reader.logger.info("精确沉默");
        mutilSessionTest(3, tag);

        MRB_Reader.logger.info("递增随机沉默");
        mutilSessionTest(4, tag);
    }

    static double mutilSessionTest(int silenceStrategy, String destTag) {
        System.out.println("strategy" + silenceStrategy);

        MRB_Reader r = new MRB_Reader();
        //标签重用
//        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
//        double avgRes = 0;
//        for (int i = 0; i < roundCount; i++) {
//            List<DataRecord> resTemp = r.MultiSession(input.MRBTagList, silenceStrategy, thresholdPM);
//            double p = resTemp.get(resTemp.size() - 1).p;
////            System.out.println(p);
//            avgRes += p;
//        }

        double avgRes = 0;
        for (int i = 0; i < roundCount; i++) {
            List<DataRecord> resTemp = r.MultiSession(MRB_TagGenerator.generateTag(tagIDlength, tagCount), silenceStrategy, thresholdPM);
            double p = resTemp.get(resTemp.size() - 1).p;
            avgRes += p;
        }

        fileUtil.transferData2Json("log/" + "s" + silenceStrategy + "_t" + thev + "_tag" + tagCount + "_r" + roundCount + destTag + ".json");

        System.out.println("avg: " + avgRes / roundCount);
        return avgRes / 10;
    }

    static double getAvgErrorProbablity(int silenceStrategy) {
        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        ArrayList<Double> errorData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            errorData.add(r.getErrorProbablity(input.MRBTagList, silenceStrategy).p4);
        }
        double sum = errorData.stream().mapToDouble(d -> d).sum();
        double avg = sum / errorData.size();
        System.out.println("沉默策略" + silenceStrategy + ",平均错误概率：" + avg);
        return avg;
    }

    static double getExactlyErrorProbablity() {
        double staticError_d = (double) staticError / 100;
        double rError_d = (double) rError / 100;
        double tError_d = (double) tError / 100;
        return 1 - (1 - staticError_d) * (1 - rError_d) * (1 - tError_d);
    }
}