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
    static int tagIDLength = 10;
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
    static int rError = 20;
    /**
     * 动态错误中从标签到读取器信息丢失概率
     */
    static int tError = 20;
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
//        int tag = 0;
//        while (tag < 40) {
//            System.out.println("round" + tag);
//            getData(String.valueOf(tag), "log/");
//            tag++;
//        }
        getThresholdSlot_1();
    }

    /**
     * 获取pm到指定收敛阈值的结果
     * 第n轮阈值：0.2/2^n（n=o~9）
     */
    public static void getThresholdSlot() {
        double threshold = 0.2;
        for (int i = 0; i < 10; i++) {
            thresholdPM = threshold;
            for (int j = 0; j <10 ; j++) {
                getData(String.valueOf(j), "log/" + i + '/');
            }
            threshold = threshold / 2;
        }
    }
    /**
     * 获取pm到指定收敛阈值的结果
     * 第n轮阈值：0.1-0.001*n（n=o~9）
     */
    public static void getThresholdSlot_1() {
        double threshold = 0.01;
        for (int i = 0; i < 10; i++) {
            thresholdPM = threshold;
            for (int j = 0; j <10 ; j++) {
                getData(String.valueOf(j), "log/" + threshold + '/');
            }
            threshold = threshold -0.001;
        }
    }

    /**
     * 获取一组模拟数据
     *
     * @param tag：添加在日志文件尾部的标签
     */
    static void getData(String tag, String destPath) {
        MRB_Reader.logger.info("随机沉默");
        mutilSessionTest(0, tag, destPath);

//        System.exit(0);

        MRB_Reader.logger.info("唯一碰撞集沉默");
        mutilSessionTest(1, tag, destPath);

        MRB_Reader.logger.info("最小唯一碰撞集沉默");
        mutilSessionTest(2, tag, destPath);

        MRB_Reader.logger.info("精确沉默");
        mutilSessionTest(3, tag, destPath);

        MRB_Reader.logger.info("递增随机沉默");
        mutilSessionTest(4, tag, destPath);
    }

    static double mutilSessionTest(int silenceStrategy, String destTag, String destPath) {
        System.out.println("strategy" + silenceStrategy);

        MRB_Reader r = new MRB_Reader();

        double avgRes = 0;
/*//        统一的标签集
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));*/
        for (int i = 0; i < roundCount; i++) {
            List<DataRecord> resTemp = r.MultiSession(Objects.requireNonNull(MRB_TagGenerator.generateTag(tagIDLength, tagCount)), silenceStrategy, thresholdPM);
            double p = resTemp.get(resTemp.size() - 1).p;
            avgRes += p;
        }

        try {
            r.fileWriterFeng.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        fileUtil.transferData2Json(destPath + "s" + silenceStrategy + "_t" + thev + "_tag" + tagCount + "_r" + roundCount + "_pm" + thresholdPM + "_" + destTag + ".json");

        System.out.println("avg: " + avgRes / roundCount);
        return avgRes / 10;
    }

    static double getAvgErrorProbability(int silenceStrategy) {
        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDLength, tagCount));
        ArrayList<Double> errorData = IntStream.range(0, 10).mapToObj(i -> r.getErrorProbability(input.MRBTagList, silenceStrategy).p4).collect(Collectors.toCollection(ArrayList::new));
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