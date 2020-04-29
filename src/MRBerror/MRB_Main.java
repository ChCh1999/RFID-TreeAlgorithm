package MRBerror;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MRB_Main {
    /**
     * 标签id长度
     */
    static int tagIDlength = 12;
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
    static int thev = 10;
    /**
     * 执行模拟的轮次
     */
    static int roundCount = 20;
    /**
     * pm收敛的阈值
     */
    static double thresholdPM = 0.000;

    public static void main(String[] args) {


        System.out.println("exactly: " + getExactlyErrorProbability());
        int tag = 0;
        while (tag < 10) {
            System.out.println("round"+tag);
            getData(String.valueOf(tag));
            tag++;
        }
    }

    /**
     * 获取一组模拟数据
     *
     * @param tag：添加在日志文件尾部的标签
     */
    static void getData(String tag) {
        MRB_Reader.logger.info("随机沉默");
        mutilSessionTest(0, tag);

//        System.exit(0);

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

        double avgRes = 0;
        //MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        for (int i = 0; i < roundCount; i++) {
            List<DataRecord> resTemp = r.MultiSession(Objects.requireNonNull(MRB_TagGenerator.generateTag(tagIDlength, tagCount)), silenceStrategy, thresholdPM);
            double p = resTemp.get(resTemp.size() - 1).p;
            avgRes += p;

//            if (i % 100 == 0) {
//                System.out.println((i+silenceStrategy*roundCount)*100/(5*roundCount));
//            }

        }

        try {
            r.fileWriterFeng.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        fileUtil.transferData2Json("log/" + "s" + silenceStrategy + "_t" + thev + "_tag" + tagCount + "_r" + roundCount + "_" + destTag + ".json");

        System.out.println("avg: " + avgRes / roundCount);
        return avgRes / 10;
    }

    static double getAvgErrorProbability(int silenceStrategy) {
        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        ArrayList<Double> errorData = IntStream.range(0, 10).mapToObj(i -> r.getErrorProbablity(input.MRBTagList, silenceStrategy).p4).collect(Collectors.toCollection(ArrayList::new));
        double sum = errorData.stream().mapToDouble(d -> d).sum();
        double avg = sum / errorData.size();
        System.out.println("沉默策略" + silenceStrategy + ",平均错误概率：" + avg);
        return avg;
    }

    static double getExactlyErrorProbability() {
        double staticErrorD = (double) staticError / 100;
        double rErrorD = (double) rError / 100;
        double tErrorD = (double) tError / 100;
        return 1 - (1 - staticErrorD) * (1 - rErrorD) * (1 - tErrorD);
    }
}