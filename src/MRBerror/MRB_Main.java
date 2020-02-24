package MRBerror;

import java.util.ArrayList;
import java.util.List;

import MRBerror.MRB_Reader.NUM;
import MRBerror.DataRecord;

public class MRB_Main {
    static int tagIDlength = 20;         // 标签id长度
    static int tagCount = 2000;             //标签数
    static int staticError = 20;         // 静态错误发生概率(百分制,下同)
    static int rError = 10;              // 动态错误中从读取器到标签信息丢失概率
    static int tError = 10;              // 动态错误中从标签到读取器信息丢失概率
    static int captureError = 0;        // 多标签响应时捕获效应发生概率
    static int thev = 100;                // Fast-Capture-Recapture标签复用率

    public static void main(String[] args) {


        System.out.println("exactly: "+getExactlyErrorProbablity());
//        for (int i = 0; i <4 ; i++) {
//            getAvgErrorProbablity(i);
//        }
        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        /*
        //ident方法
        List<NUM> resList=r.ident(input);
        System.out.println(resList);
        */

        List<DataRecord> resList=r.MultiSession(input.MRBTagList,0,0.00);
        System.out.println(resList);

//    NUM res=r.getErrorProbablity(input.MRBTagList,0);
//    System.out.println(res.p4);

/*        double[] j = new double[19];

        for (int i = 0; i < 10; i++) {
            List<MRB_Reader.NUM> thenum = new ArrayList<>();
            List<MRB_Tag> l = new ArrayList<>();
            int num = 0;
            do {
                MRB_Tag tag = new MRB_Tag(10);
                boolean hasRepeat = false;
                for (MRB_Tag t : l) {
                    if (t.ID.equals(tag.ID)) {
                        hasRepeat = true;
                        break;
                    }
                }
                if (hasRepeat == false) {
                    l.add(tag);
                    num++;
                }
            } while (num < 100);
            // 1000个标签

            MRB_Input input = new MRB_Input(l);
            MRB_Reader reader = new MRB_Reader();
            thenum = reader.ident(input);
            for (int k = 1; k < 20; k++) {
                if (thenum.get(k).p2 != 0) {
                    j[k - 1]++;
                }
            }
            thenum=null;
            System.gc();
            System.out.println(i);
        }
        System.out.print("[");
        for (double e : j) {
            e = e / 10;
            System.out.print(new java.text.DecimalFormat("#0.00000").format(e) + ",");
        }
        System.out.println("]");*/


/*
        double[] j = new double[19];
        for (int i = 0; i < 5000; i++) {
            List<MRB_Reader.NUM> thenum = new ArrayList<>();
            List<MRB_Tag> l = new ArrayList<>();
            int num = 0;
            do {
                MRB_Tag tag = new MRB_Tag(10);
                boolean hasRepeat = false;
                for (MRB_Tag t : l) {
                    if (t.ID.equals(tag.ID)) {
                        hasRepeat = true;
                        break;
                    }
                }
                if (hasRepeat == false) {
                    l.add(tag);
                    num++;
                }
            } while (num < 1000);
            // 1000个标签

            MRB_Input input = new MRB_Input(l);
            MRB_Reader reader = new MRB_Reader();
            thenum = reader.ident(input);
            for (int k = 1; k < 20; k++) {
                if (thenum.get(k).p2 != 0) {
                    j[k - 1]++;
                }
            }
            thenum=null;
            System.gc();
            System.out.println(i);
        }
        System.out.print("[");
        for (double e : j) {
            e = e / 5000;
            System.out.print(new java.text.DecimalFormat("#0.00000").format(e) + ",");
        }
        System.out.print("]");*/



/*

        List<MRB_Reader.NUM> thenum = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            List<MRB_Tag> l = new ArrayList<>();
            int num = 0;
            do {
                MRB_Tag tag = new MRB_Tag(10);
                boolean hasRepeat = false;
                for (MRB_Tag t : l) {
                    if (t.ID.equals(tag.ID)) {
                        hasRepeat = true;
                        break;
                    }
                }
                if (hasRepeat == false) {
                    l.add(tag);
                    num++;
                }
            } while (num < 1000);
            // 1000个标签

            MRB_Input input = new MRB_Input(l);
            MRB_Reader reader = new MRB_Reader();
            List<MRB_Reader.NUM> thenum2 = new ArrayList<>();
            thenum2 = reader.ident(input);
            if (thenum.isEmpty() == true) {
                thenum = thenum2;
            } else {
                for (MRB_Reader.NUM n : thenum) {
                    thenum.get(thenum.indexOf(n)).p3 += thenum2.get(thenum.indexOf(n)).p3;
                }
            }
            thenum2 = null;
            System.gc();
            System.out.println(i);
        }
        for (MRB_Reader.NUM n : thenum) {
            n.p3 = n.p3 / 500;
        }

        System.out.print("[");
        for (MRB_Reader.NUM n : thenum) {
            // 除了第一轮的数据进行输出
            if (!thenum.get(0).equals(n))
                System.out.print(new java.text.DecimalFormat("#0.00000").format(n.p3) + ",");
        }
        System.out.println("]");
/*


        System.out.print("[");
        for(int i =0;i<100;i++){
            List<MRB_Tag> l = new ArrayList<>();
            int num = 0;
            do {
                MRB_Tag tag = new MRB_Tag(10);
                boolean hasRepeat = false;
                for (MRB_Tag t : l) {
                    if (t.ID.equals(tag.ID)) {
                        hasRepeat = true;
                        break;
                    }
                }
                if (hasRepeat == false) {
                    l.add(tag);
                    num++;
                }
            }while(num<1000);
            // 1000个标签

            MRB_Input input = new MRB_Input(l);
            MRB_Reader reader = new MRB_Reader();
            MRB_Reader.NUM thenum2;
            thenum2 = reader.ident3(input);
            //System.out.print(new java.text.DecimalFormat("#0.00000").format(thenum2.p3) + ",");
            System.out.print(thenum2.p3 + ",");
            thenum2=null;
            System.gc();
        }

        System.out.print("]");


  */
/*
        double pm =1;
        System.out.print("[");
        for(int p1 = 0;p1<=90;p1++){
            for(int p2 = 0;p2<=90;p2++){
                double p = (double) p1/100 + (1-(double) p1/100)*((double) p2/100);
                int i = 0;
                do{
                    i++;
                    pm = 1-Math.pow(1-Math.pow(p,i),1000);
                }while(pm>=0.0001);
                System.out.print((double) p1/100 + "," + (double) p2/100 +"," + i + ";");
            }
        }
        System.out.print("]");
*/

    }
    static double getAvgErrorProbablity(int silenceStrategy){
        MRB_Reader r = new MRB_Reader();
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));
        ArrayList<Double> errorData = new ArrayList<Double>();
        for (int i = 0; i < 10; i++) {
            errorData.add(r.getErrorProbablity(input.MRBTagList, silenceStrategy).p4);
        }
        double sum=0;
        for (double d:errorData)sum+=d;
        double avg=sum/errorData.size();
        System.out.println("沉默策略"+silenceStrategy+",平均错误概率："+avg);
        return avg;
    }
    static double getExactlyErrorProbablity(){
        double staticError_d=(double) staticError/100;
        double rError_d=(double)rError/100;
        double tError_d=(double)tError/100;
//        return  staticError_d+(1-staticError_d)*rError_d+(1-staticError_d)*(1-rError_d)*tError_d;
        return 1-(1-staticError_d)*(1-rError_d)*(1-tError_d);
    }
}