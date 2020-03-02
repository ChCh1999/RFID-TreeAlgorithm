package MRBerror;

import java.util.ArrayList;
import java.util.List;

import MRBerror.fileUtil;
import MRBerror.MRB_Reader.NUM;
import MRBerror.DataRecord;

public class MRB_Main {
    static int tagIDlength = 10;         // 标签id长度
    static int tagCount = 1000;             //标签数
    static int staticError = 20;         // 静态错误发生概率(百分制,下同)
    static int rError = 10;              // 动态错误中从读取器到标签信息丢失概率
    static int tError = 10;              // 动态错误中从标签到读取器信息丢失概率
    static int captureError = 0;        // 多标签响应时捕获效应发生概率
    static int thev = 80;                // Fast-Capture-Recapture标签复用率
    static int silenceStrategy = 3;

    public static void main(String[] args) {


        System.out.println("exactly: " + getExactlyErrorProbablity());

        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        /*
        //ident方法
        List<NUM> resList=r.ident(input);
        System.out.println(resList);
        */

        MutilSessionTest(0);
        MutilSessionTest(1);
        MutilSessionTest(2);
        MutilSessionTest(3);


//    NUM res=r.getErrorProbablity(input.MRBTagList,0);
//    System.out.println(res.p4);
    }

    static double MutilSessionTest(int silenceStrategy) {
        System.out.println("strategy" + silenceStrategy);

        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        /*
        //ident方法
        List<NUM> resList=r.ident(input);
        System.out.println(resList);
        */


        double avgRes = 0;
        for (int i = 0; i < 1000; i++) {
            List<DataRecord> resTemp = r.MultiSession(input.MRBTagList, silenceStrategy, 0.00);
            double p = resTemp.get(resTemp.size() - 1).p;
//            System.out.println(p);
            avgRes += p;
        }

        fileUtil.transferData2Json("log/" + "s" + silenceStrategy + "_t" + thev + "_tag" + tagCount + ".json");


        System.out.println("avg: " + avgRes / 10);
        return avgRes / 10;
    }

    static double getAvgErrorProbablity(int silenceStrategy) {
        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        ArrayList<Double> errorData = new ArrayList<Double>();
        for (int i = 0; i < 10; i++) {
            errorData.add(r.getErrorProbablity(input.MRBTagList, silenceStrategy).p4);
        }
        double sum = 0;
        for (double d : errorData) sum += d;
        double avg = sum / errorData.size();
        System.out.println("沉默策略" + silenceStrategy + ",平均错误概率：" + avg);
        return avg;
    }

    static double getExactlyErrorProbablity() {
        double staticError_d = (double) staticError / 100;
        double rError_d = (double) rError / 100;
        double tError_d = (double) tError / 100;
//        return  staticError_d+(1-staticError_d)*rError_d+(1-staticError_d)*(1-rError_d)*tError_d;
        return 1 - (1 - staticError_d) * (1 - rError_d) * (1 - tError_d);
    }
}