package MRBerror;

import org.apache.log4j.Logger;

import java.util.*;

import static MRBerror.MRB_Main.*;

class DataRecord {
    //估计标签丢失概率p
    double p = 0;
    //估计的标签数目n
    double n = 0;
    //丢失任一个标签的概率
    double pm = 0;
    //本轮使用的时隙数
    int slotCount = 0;
    int countOfCaughtTag = 0;

    @Override
    public String toString() {
        return "{" +
                "\"p\":" + p +
                ", \"n\":" + n +
                ", \"pm\":" + pm +
                ", \"slotCount\":" + slotCount +
                ", \"countOfCaughtTag\":" + countOfCaughtTag +
                "}\n";
    }
}

public class MRB_Reader {
    protected static Logger logger = Logger.getLogger(MRB_Reader.class);
    private fileUtil fileWriter = new fileUtil("log/MRBRecord.txt");

    public fileUtil fileWriterFeng = new fileUtil("log/MRBRecordFeng.txt", true);
    /*
     * 往帧识别的标签集合
     */
    public List<MRB_Tag> LastFrameTagList;

    public slot slot;
    public Set<MRB_Tag> caughtTagSet;

    public MRB_Reader() {
        LastFrameTagList = new ArrayList<>();
        slot = new slot();
        caughtTagSet = new HashSet<>();
        fileWriter.clearmsg();
    }

    public void clearCaughtSet() {
        caughtTagSet = new HashSet<>();
    }

    // 内部类，用于存储计算结果
    static public class NUM {
        double p1;  // 估计概率p
        double p2;  // 统计丢失标签概率
        double p3;  // pm值
        double p4;  // 估计丢失标签概率
        int reuseCount;// 复用数目
        int slotCount;// 该轮识别所用的时隙数

        public NUM() {
            p1 = 0;
            p2 = 0;
            p3 = 0;
            p4 = 0;
        }

        public void set(double p1, double p2, double p3, double p4) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.p4 = p4;
        }

        @Override
        public String toString() {
            return String.format("NUM{p1=%.4f, p2=%.4f, p3=%.4f, p4=%.4f}", p1, p2, p3, p4);
        }
    }

    static public class resu {
        List<MRB_Tag> identifiedTagList;
        List<MRB_Tag> silentedTagList;
        List<Map<String, Set<String>>> CBMs;
        int countOfSlot;

        public resu() {
            identifiedTagList = null;
            silentedTagList = null;
            CBMs = null;
            countOfSlot = 0;
        }

        @Override
        public String toString() {
            return "resu{" +
                    "identifiedTagList=" + identifiedTagList +
                    ", silentedTagList=" + silentedTagList +
                    ", countOfSlot=" + countOfSlot +
                    ", CBMs=" + CBMs +
                    '}';
        }
    }


    // 识别方法，可靠层入口--预设轮次入口
    public List<NUM> ident(MRB_Input input) {
        List<MRB_Tag> MRBTagList = input.MRBTagList;
        List<NUM> thenum = new ArrayList<>();
        int theTagNum = MRBTagList.size();
        for (int i = 0; i < MRBTagList.size(); i++) {
            // 把ASC写入
            MRBTagList.get(i).ASC = i;
        }

        // 进行多次仲裁
        for (int i = 0; i < 20; i++) {
            // 进行仲裁
            resu r = OneFrame(MRBTagList);

            int l = 0, m = 0;     // l计算在之前周期中识别并被复用在当前周期中未识别的标签数目，m计算被复用且在当前周期识别的标签数目
            int k1 = 0, k2 = 0;
            for (MRB_Tag t : LastFrameTagList) {
                if (t.use) {
                    if (r.identifiedTagList.contains(t)) {
                        m++;
                    } else {
                        l++;
                    }
                } else {
                    k1++;
                }
            }


            // 对于本次新识别的标签，设置thev%概率复用
            //for(MRB_Tag t:r.t){
            //    int b = new Random().nextInt(100);
            //    if(b<100-MRB_Main.thev &&LastFrameTagList.contains(t)==false){
            //        t.use = false;
            //    }
            //}
            // 将本次的识别标签加入到历史识别标签中去
            for (MRB_Tag t : r.identifiedTagList) {
                if (!LastFrameTagList.contains(t)) {
                    LastFrameTagList.add(t);
                    k2++;
                }
            }

            int k = 0;
            // 对已识别的所有标签，以概率thev%复用，并消除之前帧中的复用标记
            for (MRB_Tag t : LastFrameTagList) {
                t.use = true;
                int b = new Random().nextInt(100);
                if (b < 100 - MRB_Main.thev && k < 963) {
                    t.use = false;
                    k++;
                }
            }
            double p1;       // 计算每轮丢失标签概率估计值p1
            if (l == 0 && m == 0) {
                p1 = 1;
            } else {
                p1 = (double) l / (double) (l + m);
            }
            double p2 = 0;        //  到本次为止，实际的标签丢失比例
            // 值得注意的是，到本轮的标签丢失比例不是p1的实际值
            p2 = (double) 1 - (double) LastFrameTagList.size() / (double) theTagNum;

            // 将计算结果保存
            NUM num = new NUM();
            if (thenum.isEmpty()) {
                // 第一轮
                double p3 = 1;
                double p4 = r.countOfSlot;
                num.set(p1, p2, p3, p4);
            } else {
                int c = thenum.size();
                if (c > 1) { // 去除第一轮循环的数据p1对整体的影响（该p1无意义）
                    p1 = (p1 + c * thenum.get(c - 1).p1) / (double) (c + 1);
                }
                // 估计N值
                double theN = ((double) (k1 + k2 + l + m)) / ((double) 1 - Math.pow(p1, i + 1));
                //System.out.println(theN);
                double p3 = 1 - Math.pow(1 - Math.pow(p1, i + 1), (int) theN);
                //double p4 = Math.pow(p1,i+1);
                double p4 = (double) r.countOfSlot + thenum.get(c - 1).p4;
                num.set(p1, p2, p3, p4);
            }
            // p3是要计算的那个估计值,p4是时隙数目
            thenum.add(num);
        }
        return thenum;
    }

    // MRCT
    public NUM ident2(MRB_Input input) {
        List<MRB_Tag> MRBTagList = input.MRBTagList;
        int theTagNum = MRBTagList.size();
        for (int i = 0; i < MRBTagList.size(); i++) {
            // 把ASC写入
            MRBTagList.get(i).ASC = i;
        }
        int thenum = 0;
        NUM n = new NUM();
        do {
            resu r = OneFrame(MRBTagList);
            for (MRB_Tag t : r.identifiedTagList) {
                if (LastFrameTagList.contains(t) == false) {
                    LastFrameTagList.add(t);
                    t.use = false;
                }
            }
            thenum = r.identifiedTagList.size();
            n.p2 = (double) LastFrameTagList.size() / (double) theTagNum;
            n.p4 += r.countOfSlot;
        } while (thenum != 0);
        return n;
    }

    // 基于捕获-再捕获技术的快速识别协议
    public NUM ident3(MRB_Input input) {
        List<MRB_Tag> MRBTagList = input.MRBTagList;
        NUM n1 = new NUM();
        int theTagNum = MRBTagList.size();
        for (int i = 0; i < MRBTagList.size(); i++) {
            // 把ASC写入
            MRBTagList.get(i).ASC = i;
        }
        double pm = 1;
        double p2;        //  到本次为止，实际的标签丢失比例
        int c = 0;
        do {
            resu r = OneFrame(MRBTagList);
            //System.out.println(re.size());
            int l = 0, m = 0;     // l计算在之前周期中识别并被复用在当前周期中未识别的标签数目，m计算被复用且在当前周期识别的标签数目
            int k1 = 0, k2 = 0;
            for (MRB_Tag t : LastFrameTagList) {
                if (t.use == true) {
                    if (r.identifiedTagList.contains(t)) {
                        m++;
                    } else {
                        l++;
                    }
                } else {
                    k1++;
                }
            }

            // 将本次的识别标签加入到历史识别标签中去
            for (MRB_Tag t : r.identifiedTagList) {
                if (LastFrameTagList.contains(t) == false) {
                    LastFrameTagList.add(t);
                    k2++;
                }
            }
            // 对已识别的所有标签，以概率thev%复用，并消除之前帧中的复用标记
            for (MRB_Tag t : LastFrameTagList) {
                t.use = true;
                int b = new Random().nextInt(100);
                if (b < 100 - MRB_Main.thev) {
                    t.use = false;
                }
            }
            double p1 = 0;       // 计算本次丢失标签概率估计值p1
            if (l == 0 && m == 0) {
                p1 = 1;
            } else {
                p1 = (double) l / (double) (l + m);
            }
            p2 = (double) 1 - (double) LastFrameTagList.size() / (double) theTagNum;
            if (c == 0) {
                n1.set(p1, pm, c, r.countOfSlot);
            } else {
                if (c > 1)
                    p1 = (c * n1.p1 + p1) / (double) (c + 1);
                // 估计N值
                double theN = ((double) (k1 + k2 + l + m)) / ((double) 1 - Math.pow(p1, c + 1));
                //System.out.println(theN);
                pm = 1 - Math.pow(1 - Math.pow(p1, c + 1), (int) theN);
                //double p4 = Math.pow(p1,i+1);
                double p4 = (double) r.countOfSlot + n1.p4;
                n1.set(p1, pm, c + 1, p4);
            }
            c++;
        } while (pm > 0.00001);
        return n1;
    }


    // 组合估计器在MRB算法基础上应用
    public NUM ident4(MRB_Input input) {
        List<MRB_Tag> MRBTagList = input.MRBTagList;
        NUM n1 = new NUM();
        int theTagNum = MRBTagList.size();
        for (int i = 0; i < MRBTagList.size(); i++) {
            // 把ASC写入
            MRBTagList.get(i).ASC = i;
        }
        double pm = 1;
        int c = 0;
        double p2;
        // 存储之前识别周期的n、m、M值
        List<Integer> M = new ArrayList<Integer>();
        List<Integer> N = new ArrayList<Integer>();
        List<Integer> m = new ArrayList<Integer>();
        do {
            resu r = OneFrame(MRBTagList);
            // 每个识别周期中识别标签数目n，每个识别周期中再次识别（已被识别）的标签数目m，每个周期中在此周期之前已被识别的标签数目M
            int n = 0, m1 = 0, M1 = 0;
            M1 = LastFrameTagList.size();
            n = r.identifiedTagList.size();
            for (MRB_Tag t : r.identifiedTagList) {
                if (LastFrameTagList.contains(t) == false) {
                    m1++;
                    LastFrameTagList.add(t);
                }
            }
            M.add(M1);
            N.add(n);
            m.add(m1);
            int thenM = 0;
            int them = 0;
            for (int i = 0; i <= c; i++) {
                thenM += M.get(i) * N.get(i);
                them += m.get(i);
            }
            // 计算N值第一次估计
            double theNS = (double) thenM / (double) them;
            // 计算p值第一次估计
            double thep = 0;
            for (int i = 0; i <= c; i++) {
                thep += 1 - (double) N.get(i) / theNS;
            }
            thep = thep / (double) (c + 1);

            // 进行第二次估计
            int thek = LastFrameTagList.size();    // k值
            // 计算第二次估计的NS值
            theNS = (double) thek / ((double) 1 - Math.pow(thep, c + 1));
            // 计算第二次估计的p值
            thep = 0;
            for (int i = 0; i <= c; i++) {
                thep += 1 - (double) N.get(i) / theNS;
            }
            thep = thep / (double) (c + 1);
            // 计算最后的pm值
            if (c >= 1)
                pm = 1 - Math.pow(1 - Math.pow(thep, c + 1), theNS);
            p2 = (double) 1 - (double) LastFrameTagList.size() / (double) theTagNum;
            if (c == 0) {
                n1.set(c, p2, r.countOfSlot, pm);
            } else {
                n1.set(c, p2, n1.p3 + r.countOfSlot, pm);
            }

            c++;
        } while (pm > 0.98);
        return n1;
    }


    // Schnabel估计器，低性能，弃置
    public NUM ident5(MRB_Input input) {
        List<MRB_Tag> MRBTagList = input.MRBTagList;
        NUM n1 = new NUM();
        int theTagNum = MRBTagList.size();
        for (int i = 0; i < MRBTagList.size(); i++) {
            // 把ASC写入
            MRBTagList.get(i).ASC = i;
        }
        double pm = 1;
        int c = 0;
        // 存储之前识别周期的n、m、M值
        List<Integer> M = new ArrayList<Integer>();
        List<Integer> N = new ArrayList<Integer>();
        List<Integer> m = new ArrayList<Integer>();
        do {
            resu r = OneFrame(MRBTagList);
            // 每个识别周期中识别标签数目n，每个识别周期中再次识别（已被识别）的标签数目m，每个周期中在此周期之前已被识别的标签数目M
            int n = 0, m1 = 0, M1 = 0;
            M1 = LastFrameTagList.size();
            n = r.identifiedTagList.size();
            for (MRB_Tag t : r.identifiedTagList) {
                if (LastFrameTagList.contains(t) == false) {
                    m1++;
                    LastFrameTagList.add(t);
                }
            }
            M.add(M1);
            N.add(n);
            m.add(m1);
            int thenM = 0;
            int them = 0;
            for (int i = 0; i <= c; i++) {
                thenM += M.get(i) * N.get(i);
                them += m.get(i);
            }
            // 计算N值第一次估计
            double theNS = (double) thenM / (double) them;
            // 计算p值第一次估计
            double thep = 0;
            for (int i = 0; i <= c; i++) {
                if (theNS > 0)
                    thep += 1 - (double) N.get(i) / theNS;
            }
            thep = thep / (double) (c + 1);
            // 较组合估计器而言，其无第二次估计
            if (c >= 1)
                pm = 1 - Math.pow(1 - Math.pow(thep, c + 1), theNS);
            double p2;        //  到本次为止，实际的标签丢失比例
            p2 = (double) 1 - (double) LastFrameTagList.size() / (double) theTagNum;
            if (c == 0) {
                n1.set(thep, p2, r.countOfSlot, pm);
            } else {
                n1.set(thep, p2, n1.p3 + r.countOfSlot, pm);
            }
            c++;
        } while (pm > 0.2);
        return n1;
    }


    // 仲裁层入口 弃置
    public resu OneFrame(List<MRB_Tag> l) {
        // 这一帧已识别标签ID集合
        List<String> currentFrameTagList = new ArrayList<>();
        // 这一帧待识别标签集合
        List<MRB_Tag> MRBTagList = l;
        // 这一帧未发生静态错误的标签集合，用于标签传输阶段
        List<MRB_Tag> TheTrueTag = new ArrayList<>();

        // 结果反馈
        resu res = new resu();

        // 初始化MRBTagList标签列表，之前帧中设定沉默的标签被筛除
        // 这儿是修改部分，修改了这儿导致运行卡住。需要检查是为什么
        //for(MRB_Tag t:l){
        //    if(t.use==true){
        //        MRBTagList.add(t);
        //    }
        //}


        // 生成0-100的随机数,当大于等于20时存入NowTag，模拟20%概率在本次读写器周期内丢失标签（静态错误）
        for (MRB_Tag t : MRBTagList) {
            int a = new Random().nextInt(100);
            if (a >= staticError) {
                TheTrueTag.add(t);
            }
        }

        // 此处的标签ID长度即为论文中SN长度
        int tagIDLength = MRBTagList.get(0).ID.length();

        logger.info("----------帧开始----------");

        // 初始化输出信息
        int slotNum = 0;
        int totalBitNum = 0;
        double totalTime = 0.0;

        // CUCS是ASC的集合
        Set<Integer> CUCS = new HashSet<>();
        Map<String, Set<String>> PCB = new HashMap<>();
        Map<String, Set<String>> CCB = new HashMap<>();

        // 初始化CCB
        String firstID = MRBTagList.get(0).ID;
        Set<String> CCBValue = new HashSet<>();
        CCBValue.add(firstID);
        CCB.put(firstID, CCBValue);
        // 初始化PCB
        PCB.put("NS", new HashSet<>());

        int i = 0;
        int N = MRBTagList.size();
        int d = 0; // 用于统计跳过的被沉默的标签的个数，这些被沉默标签数目应当在通信时加入到groupsize中，并重置
        while (i < N) {
            // 如果遇到被沉默（未被复用的标签），则分析时跳过该标签
            // 注意到通信时依然会发消息给包含该标签的集合，此时该标签不接受响应
            if (MRBTagList.get(i).use == false) {
                i++;
                d++;
                continue;
            }
            logger.info("----------增加标签" + MRBTagList.get(i).ID + ";    i = " + i + "----------");
            logger.info("-----更新CCB-----");
            boolean hasDumplicate = false;
            CCB.clear();
            for (String s : PCB.keySet()) {
                String SN_i = MRBTagList.get(i).ID;
                Set<String> tags = PCB.get(s);

                Set<String> tagsClone = new HashSet<>();
                tagsClone.addAll(tags);
                tagsClone.add(SN_i);

                // 进行判断，如果CCB值重复，也不可以
                if (CCB.containsKey(CB(s, SN_i))) {
                    hasDumplicate = true;
                } else {
                    CCB.put(CB(s, SN_i), tagsClone);
                }
            }

            printCCBandPCB("CCB", CCB);

            for (String s1 : CCB.keySet()) {
                for (String s2 : PCB.keySet()) {
                    if (s1.equals(s2)) hasDumplicate = true;
                }
            }

            if (hasDumplicate) {
                slotNum++;
                logger.info("-----CCB和PCB有重复，不再增加标签-----");
                int groupSize = CUCS.size();
                // 加入被跳过的标签，使得响应列表连续
                groupSize += d;
                d = 0;
                CUCS.clear();
                CCB.clear();
                // 在PCB被重置之前根据PCB完成操作
                logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
                logger.info("---------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID-----------------");

                slot.broadcast = (i - groupSize) + "|" + groupSize;

                // 标签接受广播并进行响应
                MRB_Transmission.Broadcast(this, TheTrueTag);
                // 读取器接受标签响应
                MRB_Transmission.receiveReplies(this, TheTrueTag);
                System.out.println("receive msg from tag: " + this.slot.receive);
                logger.info("receive msg from tag: " + this.slot.receive);
                int num = 0;   // 统计识别的标签数目
                String s2 = "";
                List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
                // 读取器处理标签响应，并将识别得到的标签存入已识别列表
                for (String s : PCB.keySet()) {
                    if (s.equals(this.slot.receive)) {
                        logger.info("识别标签" + PCB.get(s));
                        System.out.println("识别标签" + PCB.get(s));
                        for (String st : PCB.get(s)) {
                            currentFrameTagList.add(st);
                            num++;
                            s2 = st;
                        }
                    }
                }

                this.slot.clear();

                /*

                // 添加对捕获效应处理

                if(num==1){
                    // 寻找到该时隙内唯一识别的标签，并将其沉默
                    for(MRB_Tag t:MRBTagList){
                        if(t.ID == s2){
                            t.use=false;
                            t3.add(t);
                        }
                    }

                    slot.broadcast = String.valueOf(i-groupSize) + "|" + String.valueOf(groupSize);
                    // 标签接受广播并进行响应
                    MRB_Transmission.Broadcast(this,TheTrueTag);
                    // 读取器接受标签响应
                    MRB_Transmission.receiveReplies(this,TheTrueTag);
                    s2="";
                    slotNum++;
                    num=0;
                    // 读取器处理标签响应，并将识别得到的标签存入已识别列表
                    for(String s:PCB.keySet()){
                        if(s.equals(this.slot.receive)){
                            for(String st:PCB.get(s)){
                                currentFrameTagList.add(st);
                                num++;
                                s2 = st;
                            }
                        }
                    }
                    this.slot.clear();
                };
                // 将沉默标签恢复可用
                for(MRB_Tag t:t3){
                    t.use = true;
                }
                t3.clear();




                 */


                PCB.clear();
                // 初始化CCB
                String currentID = MRBTagList.get(i).ID;
                Set<String> CurrCCBValue = new HashSet<>();
                CCBValue.add(currentID);
                CCB.put(currentID, CurrCCBValue);

                // 初始化PCB
                PCB.put("NS", new HashSet<>());

            } else {
                logger.info("-----CCB和PCB没重复，增加标签-----");
                PCB.putAll(CCB);
                CUCS.add(i);
                i++;
                logger.info("-----结束时状态-----");
            }

            printCUCS(CUCS);
            printCCBandPCB("CCB", CCB);
            printCCBandPCB("PCB", PCB);
        }

        logger.info("----------最后一个标签 i = " + i + "----------");

        int groupSize = CUCS.size();
        groupSize += d;
        logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
        logger.info("----------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID------------------");

        slot.broadcast = (i - groupSize) + "|" + groupSize;

        // 标签接受广播并进行响应
        MRB_Transmission.Broadcast(this, TheTrueTag);
        // 读取器接受标签响应
        MRB_Transmission.receiveReplies(this, TheTrueTag);

        System.out.println("receive msg from tag: " + this.slot.receive);
        logger.info("receive msg from tag: " + this.slot.receive);
        int num = 0;   // 统计识别的标签数目
        String s2 = "";
        List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
        // 读取器处理标签响应，并将识别得到的标签存入已识别列表
        for (String s : PCB.keySet()) {
            if (s.equals(this.slot.receive)) {
                logger.info("识别标签" + PCB.get(s));
                System.out.println("识别标签" + PCB.get(s));
                for (String st : PCB.get(s)) {
                    currentFrameTagList.add(st);
                    num++;
                    s2 = st;
                }
            }
        }

        this.slot.clear();


        // 添加对捕获效应处理
        /*

        if(num==1){
            // 寻找到该时隙内唯一识别的标签，并将其沉默
            for(MRB_Tag t:MRBTagList){
                if(t.ID == s2){
                    t.use=false;
                    t3.add(t);
                }
            }

            slot.broadcast = String.valueOf(i-groupSize) + "|" + String.valueOf(groupSize);
            // 标签接受广播并进行响应
            MRB_Transmission.Broadcast(this,TheTrueTag);
            // 读取器接受标签响应
            MRB_Transmission.receiveReplies(this,TheTrueTag);
            s2="";
            num=0;
            slotNum++;
            // 读取器处理标签响应，并将识别得到的标签存入已识别列表
            for(String s:PCB.keySet()){
                if(s.equals(this.slot.receive)){
                    for(String st:PCB.get(s)){
                        currentFrameTagList.add(st);
                        num++;
                        s2 = st;
                    }
                }
            }
            this.slot.clear();
        };
        // 将沉默标签恢复可用
        for(MRB_Tag t:t3){
            t.use = true;
        }
        t3.clear();




         */
        slotNum++;

        logger.info("---------这轮识别结束，共消耗" + slotNum + "个时隙");
        logger.info("---------已识别标签列表如下：");
        for (String s : currentFrameTagList) {
            logger.info(s);
        }
        // 将string换成对应的MRB_Tag
        List<MRB_Tag> result = new ArrayList<>();
        for (MRB_Tag t : MRBTagList) {
            if (currentFrameTagList.contains(t.ID)) {
                result.add(t);
            }
        }
        res.identifiedTagList = result;
        res.countOfSlot = slotNum;
        return res;
    }

    /**
     * 重载识别过程 指定沉默策略,重置沉默标签
     *
     * @param l               标签集合
     * @param silenceStrategy 沉默策略序号 1.随机沉默
     * @return resu (包括识别的标签 、 沉默的标签 、 识别用的时隙数等数据)
     */
    public resu OneFrame(List<MRB_Tag> l, int silenceStrategy) {
        // 这一帧已识别标签ID集合
        List<String> currentFrameTagList = new ArrayList<>();
        // 这一帧未发生静态错误的标签集合，用于标签传输阶段
        List<MRB_Tag> TheTrueTag = new ArrayList<>();
        //存储唯一碰撞集
        List<Map<String, Set<String>>> CBMs = new ArrayList<>();
        //存储唯一碰撞集中的标签
        List<List<MRB_Tag>> CBMTagList = new ArrayList<>();
        //被沉默的标签
        List<MRB_Tag> silencedTagList = new ArrayList<>();


        // 生成0-100的随机数,当大于等于20时存入NowTag，模拟20%概率在本次读写器周期内丢失标签（静态错误）
        for (MRB_Tag t : l) {
            int a = new Random().nextInt(100);
            if (a >= staticError) {
                TheTrueTag.add(t);
            } else {
                logger.error("****标签:" + t + "发生静态错误****");
            }
        }

/*        // 此处的标签ID长度即为论文中SN长度
        int tagIDLength = MRBTagList.get(0).ID.length();*/

        logger.info("----------帧开始----------");

        // 时隙消耗统计
        int slotNum = 0;
//        int totalBitNum = 0;
//        double totalTime = 0.0;

        // CUCS是ASC的集合
        Set<Integer> CUCS = new HashSet<>();

        // 初始化CCB
        Map<String, Set<String>> CCB = new HashMap<>();
        List<MRB_Tag> tagInCCB = new ArrayList<>();

        // 初始化PCB
        Map<String, Set<String>> PCB = new HashMap<>();
        PCB.put("NS", new HashSet<>());

        int i = 0;
        int N = l.size();
        // 用于统计跳过的被沉默的标签的个数，这些被沉默标签数目应当在通信时加入到groupsize中，并重置
        int d = 0;

        // 开始识别
        while (i < N) {
            // 如果遇到被沉默（未被复用的标签），则分析时跳过该标签
            // 注意到通信时依然会发消息给包含该标签的集合，此时该标签不接受响应
            if (!l.get(i).use) {
                silencedTagList.add(l.get(i));
                i++;
                d++;
                continue;
            }
            logger.info("----------增加标签" + l.get(i).ID + "----------");
            logger.info("i = " + i);
//            logger.info("-----更新CCB-----");
            boolean hasDumplicate = false;
            CCB.clear();
            for (String s : PCB.keySet()) {
                String SN_i = l.get(i).ID;
                Set<String> tags = PCB.get(s);

                Set<String> tagsClone = new HashSet<>(tags);
                tagsClone.add(SN_i);

                // 进行判断，如果CCB值重复，也不可以
                if (CCB.containsKey(CB(s, SN_i))) {
                    hasDumplicate = true;
                    logger.info("duplicate key in CCB: " + CB(s, SN_i));
                } else {
                    CCB.put(CB(s, SN_i), tagsClone);
                }

            }
//            printCCBandPCB("CCB", CCB);

            for (String s1 : CCB.keySet()) {
                for (String s2 : PCB.keySet()) {
                    if (s1.equals(s2)) {
                        hasDumplicate = true;
                        logger.info("duplicate key in CCB-PCB: " + s1);
                        break;
                    }
                }
            }

            if (hasDumplicate) {
                slotNum++;
                logger.info("-----CCB和PCB有重复，不再增加标签-----");
                printCCBandPCB("CCB", CCB);
                printCCBandPCB("PCB", PCB);
                printCUCS(CUCS);
                int groupSize = CUCS.size();
                // 加入被跳过的标签，使得响应列表连续
                groupSize += d;
                d = 0;
                Map<String, Set<String>> PCBcopy = new HashMap<>(PCB);


                // 在PCB被重置之前根据PCB完成操作
                logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
                logger.info("---------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID-----------------");

                slot.broadcast = (i - groupSize) + "|" + groupSize;

                // 标签接受广播并进行响应
                MRB_Transmission.Broadcast(this, TheTrueTag);
                // 读取器接受标签响应
                MRB_Transmission.receiveReplies(this, TheTrueTag);
                logger.info("receive msg from tag: " + this.slot.receive);
                // 统计识别的标签数目
                int num = 0;
                String s2 = "";
                List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();

                // 读取器处理标签响应，并将识别得到的标签存入已识别列表
                for (String s : PCB.keySet()) {
                    if (s.equals(this.slot.receive)) {
                        logger.info("识别标签" + PCB.get(s));
                        for (String st : PCB.get(s)) {
                            currentFrameTagList.add(st);
                            num++;
                            s2 = st;
                        }
                    }
                }

                //存储数据
                CBMs.add(PCBcopy);
                CBMTagList.add(tagInCCB);

                //重置数据
                CUCS.clear();
                CCB.clear();
                this.slot.clear();
                PCB.clear();

                // 初始化CCB
                String currentID = l.get(i).ID;
                Set<String> CurrCCBValue = new HashSet<>();
                tagInCCB = new ArrayList<>();
                CCB.put(currentID, CurrCCBValue);

                // 初始化PCB
                PCB.put("NS", new HashSet<>());
                logger.info("-----重置状态-----");
                logger.info("");
            } else {
                tagInCCB.add(l.get(i));
                PCB.putAll(CCB);
                CUCS.add(i);
                i++;
            }


        }

        //最后一个标签
        {
            logger.info("----------最后一个标签----------");
            int groupSize = CUCS.size();
            groupSize += d;

            logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
            logger.info("----------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID------------------");

            slot.broadcast = (i - groupSize) + "|" + groupSize;

            // 标签接受广播并进行响应
            MRB_Transmission.Broadcast(this, TheTrueTag);
            // 读取器接受标签响应
            MRB_Transmission.receiveReplies(this, TheTrueTag);

            logger.info("receive msg from tag: " + this.slot.receive);
            // 统计识别的标签数目
            int num = 0;
            String s2 = "";
            List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
            // 读取器处理标签响应，并将识别得到的标签存入已识别列表
            for (String s : PCB.keySet()) {
                if (s.equals(this.slot.receive)) {
                    logger.info("识别标签" + PCB.get(s));
                    for (String st : PCB.get(s)) {
                        currentFrameTagList.add(st);
                        num++;
                        s2 = st;
                    }
                }
            }

            //存储数据
            Map<String, Set<String>> PCBcopy = new HashMap<>(PCB);
            CBMs.add(PCBcopy);
            CBMTagList.add(tagInCCB);

            this.slot.clear();

        }

        /*
         **********************************************结束识别
         */

        //记录识别的标签
        // 将string换成对应的MRB_Tag
        List<MRB_Tag> result = new ArrayList<>();
        for (MRB_Tag t : l) {
            if (currentFrameTagList.contains(t.ID)) {
                result.add(t);
                caughtTagSet.add(t);
            }
        }

        //根据参数对标签进行沉默
        //至少需要沉默的标签数
        int toSilentCount = (100 - thev) * caughtTagSet.size() / 100;
        //至少需要沉默的标签数
        int silentCount = toSilentCount;

        //将要沉默的标签的ID集合
        List<String> toSilenceTagIds = new ArrayList<String>();

        //本轮将要沉默的标签
        List<MRB_Tag> toSilenceTagList = new ArrayList<>();
        //已捕获的标签的列表 主要用于随机访问
        List<MRB_Tag> caughtTagList = new ArrayList<>(caughtTagSet);

        //若采取递增式沉默且识别标签不足，则沉默本轮识别的所有标签
        if (toSilentCount > currentFrameTagList.size()+silencedTagList.size() && silenceStrategy > 0 && silenceStrategy <= 4) {
            silenceStrategy = -1;
        }
        //根据沉默标签进行沉默选择
        switch (silenceStrategy) {
            default:
            case -1:
                toSilenceTagIds.addAll(currentFrameTagList);
                break;
            case 0:
                //随机沉默

                //重置已沉默标签
                for (MRB_Tag tag : silencedTagList) {
                    tag.use = true;
                }
                //沉默的序号，在caughtTagList中定位
                List<Integer> toSilenceIndex = new ArrayList<Integer>();
                silencedTagList = new ArrayList<>();
                int tagCount = caughtTagList.size();
                Random random = new Random();
                //随机选择沉默的标签序号
                for (int silentIndex = 0; silentIndex < silentCount; silentIndex++) {
                    //随机生成沉默序号
                    int index = -1;
                    do {
                        index = random.nextInt(tagCount);
                    } while (toSilenceIndex.contains(index));
                    toSilenceIndex.add(index);
                    //添加index的ID
                    toSilenceTagIds.add(caughtTagList.get(index).ID);
                }
                break;
            case 2:
                //从小的CBM沉默
                CBMTagList.sort(Comparator.comparing(List::size));
            case 1:
                //以CBM为单位沉默
                //添加已沉默标签
                silentCount = silentCount - silencedTagList.size();
                if (silentCount <= 0) {
                    break;
                }
                //记录筛选出的待沉默标签数
                int addCount = 0;
                //优先沉默全识别的唯一碰撞集
                for (List<MRB_Tag> CBMTags : CBMTagList) {
                    if (addCount < silentCount) {
                        boolean catchAll = true;
                        for (MRB_Tag tag : CBMTags) {
                            if (!currentFrameTagList.contains(tag.ID)) {
                                catchAll = false;
                                break;
                            }
                        }
                        if (catchAll) {
                            CBMTags.forEach(tag -> toSilenceTagIds.add(tag.ID));
                            addCount += CBMTags.size();
                        }
                    }
                }
                //沉默剩余唯一碰撞集以补全沉默标签
                int point = 0;
                while (addCount < silentCount) {
                    List<MRB_Tag> CBMTags = CBMTagList.get(point);
                    //将未沉默的部分加入到沉默标签集
                    if (!toSilenceTagIds.contains(CBMTags.get(0).ID)) {
                        for (MRB_Tag tag : CBMTags) {
                            if(caughtTagSet.contains(tag)) {
                                toSilenceTagIds.add(tag.ID);
                                addCount +=1;
                            }
                        }

                    }
                    point++;
                }
                break;
            case 3:
                //精确沉默
                //添加已沉默标签
                silentCount = silentCount - silencedTagList.size();
                if (silentCount <= 0) {
                    break;
                }

                int cbm_index = 0;
                //循环直到找齐
                while (toSilenceTagIds.size() < silentCount) {
                    //每次取出一个CBM中的第一个
                    List<MRB_Tag> CBMTagsTemp = CBMTagList.get(cbm_index);
                    for (MRB_Tag tag : CBMTagsTemp) {
                        if (tag.use && caughtTagList.contains(tag)) {
                            if(!toSilenceTagIds.contains(tag.ID)) {
                                toSilenceTagIds.add(tag.ID);
                                break;
                            }
                        }
                    }
                    cbm_index = (cbm_index + 1) % CBMs.size();
                }
                break;
            case 4:
                //随机沉默-递增方案
                silentCount = silentCount - silencedTagList.size();
                if (silentCount <= 0) {
                    break;
                }
                // 沉默的序号，在currentFrameTagList中定位
                toSilenceIndex = new ArrayList<>();
                tagCount = currentFrameTagList.size();
                random = new Random();
                //随机选择沉默的标签序号
                addCount = 0;
                while (addCount < silentCount) {
                    //随机生成沉默序号
                    int index = -1;
                    do {
                        index = random.nextInt(tagCount);
                    } while (toSilenceIndex.contains(index));
                    toSilenceIndex.add(index);
                    //添加index的ID
                    toSilenceTagIds.add(currentFrameTagList.get(index));
                    addCount++;
                }
                break;
        }

        //沉默相应标签
        int failCount=0;
        for (MRB_Tag tag : l) {
            if (!tag.use) {
                toSilenceTagList.add(tag);
                continue;
            }
            if (toSilenceTagList.size() < toSilentCount && toSilenceTagIds.contains(tag.ID) && caughtTagSet.contains(tag)) {
                tag.use = false;
                logger.info("沉默标签" + tag.ID);
                toSilenceTagList.add(tag);
            }
            if(toSilenceTagList.size() < toSilentCount && toSilenceTagIds.contains(tag.ID)){
                if(!caughtTagSet.contains(tag)){
                    failCount++;
                }
            }
        }

        slotNum++;

        logger.info("---------这轮识别结束，共消耗" + slotNum + "个时隙");
        logger.info("---------已识别标签列表如下：");
        for (String s : currentFrameTagList) {
            logger.info(s);
        }

        // 结果反馈
        resu res = new resu();
        res.identifiedTagList = result;
        res.silentedTagList = toSilenceTagList;
        res.countOfSlot = slotNum;
        res.CBMs = CBMs;
        return res;
    }

    /**
     * 通过两轮识别估计错误概率
     *
     * @param l
     * @param silenceStrategy
     * @return
     */
    public NUM getErrorProbability(List<MRB_Tag> l, int silenceStrategy) {
        NUM result = new NUM();
        MRB_Reader.resu res = OneFrame(l, silenceStrategy);
        MRB_Reader.resu res2 = OneFrame(l, silenceStrategy);
        int missFrame2 = 0, caughtFrame2 = 0;
        List<MRB_Tag> reuseTagList = new ArrayList<>(res.identifiedTagList);
        for (MRB_Tag tag : res.silentedTagList) {
            reuseTagList.remove(tag);
        }
        for (MRB_Tag tag : reuseTagList) {
            if (res2.identifiedTagList.contains(tag)) {
                caughtFrame2++;
            } else {
                missFrame2++;
            }
        }
//        System.out.println("第二次未捕获的标签"+missFrame2);
//        System.out.println("第二次重用的标签"+reuseTagList.size());
        result.slotCount = res.countOfSlot;
        if (reuseTagList.size() == 0) {
            result.p4 = 1;
        } else {
            result.p4 = (double) missFrame2 / reuseTagList.size();
        }
        return result;
    }

    /**
     * 通过多轮识别进行估计
     *
     * @param mrb_tags        参与识别的标签集合
     * @param silenceStrategy 沉默策略
     * @param thresholdOfPM   pm的阈值
     * @return 估计结果
     */
    public List<DataRecord> MultiSession(List<MRB_Tag> mrb_tags, int silenceStrategy, double thresholdOfPM) {

        fileWriterFeng.writemsg("\n---------------------------new MultiSession------------------------------");
        fileWriterFeng.writemsg("\nsilenceStrategy:" + silenceStrategy + "\n");

        double pm = 1;
        //重置已捕获的标签记录
        clearCaughtSet();
        //记录结果
        ArrayList<DataRecord> resList = new ArrayList<>();
        //R 记录循环轮次
        int R = 0;
        //重置标签
        for (MRB_Tag tag : mrb_tags) {
            tag.use = true;
        }
        //第0轮识别
        int totalSilentedCount = 0;
        resu lastFrame = OneFrame(mrb_tags, silenceStrategy);
        //保存结果
        DataRecord res = new DataRecord();


        //pm
        res.pm = pm;
        //总的识别标签数
        res.countOfCaughtTag = caughtTagSet.size();
        resList.add(res);

        fileWriter.writemsg(",[\n");
        while (pm > thresholdOfPM && R < 20) {

            resu thisFrame = OneFrame(mrb_tags, silenceStrategy);

            //统计数据
            // 两次均找到的标签数  对应m
            int m = 0;
            //沉默标签数 k1
            int k1 = 0;
            //重用但没找到的标签数 l=上轮识别-上轮沉默-两轮均识别
            int l = 0;
            //上轮未识别这轮新识别的标签 k2
            int k2 = 0;
            //计数 CommonTagCount
            for (MRB_Tag tag : caughtTagSet) {
                if (lastFrame.silentedTagList.contains(tag)) {
                    k1++;  //上轮被沉默的标签
                }
                //参与本次识别的标签
                else if (!thisFrame.identifiedTagList.contains(tag)) {
                    l++;//本轮未被识别的标签
                }
                //本轮被识别的标签
                else {
                    m++;//本轮被识别的标签
                }
            }
            k2 = caughtTagSet.size() - resList.get(resList.size() - 1).countOfCaughtTag;
            m = m - k2;

            //估计结果

            //错误概率p的估计值 p=l/l+m
            double p = (double) l / (l + m);
            //p与之前结果取均值
            for (DataRecord dataRecord : resList) {
                p = p + dataRecord.p;
            }
            p = p / (resList.size());

            //参与识别的标签数N的估计值
            double N = ((double) (k1 + k2 + l + m))
                    / ((double) 1 - Math.pow(p, R + 1));
            //pm的估计值
            pm = 1 - Math.pow(1 - Math.pow(p, R + 1), (int) N);

            fileWriterFeng.writemsg(""
                    + "lastFrameCBMsSize:" + lastFrame.CBMs.size()
                    + ",thisFrameCBMsSize:" + thisFrame.CBMs.size()
                    + "\n"
            );


            int CBMCount = thisFrame.CBMs.size();
            int sameCBMCount = figureSameCBMCount(thisFrame.CBMs, lastFrame.CBMs);

//            if (sameCBMCount[0] != sameCBMCount[1]){
//                System.out.println("there is!"+" key:"+sameCBMCount[0]+" all:"+sameCBMCount[1]);
////                System.exit(0);
//            }


            //保存结果
            res = new DataRecord();
            //估计标签数
            res.n = N;
            //更新沉默标签数
            totalSilentedCount = thisFrame.silentedTagList.size();
            //估计丢失标签概率
            res.p = p;
            //时隙数
            res.slotCount = thisFrame.countOfSlot;
            //pm
            res.pm = pm;
            //总的识别标签数
            res.countOfCaughtTag = caughtTagSet.size();
            resList.add(res);
            R++;

            //写入json格式日志
            if (R != 1) {
                fileWriter.writemsg(",");
            }
            fileWriter.writemsg(
                    "{" +
                            "\"k1\":" + k1
                            + ", \"k2\":" + k2
                            + ", \"l\":" + l
                            + ", \"m\":" + m
                            + ", \"p\":" + p
                            + ", \"n\":" + N
                            + ", \"pm\":" + pm
                            + ", \"caught\":" + caughtTagSet.size()
                            + ", \"slot\":" + thisFrame.countOfSlot
                            + ", \"CBMCount\":" + CBMCount
                            + ", \"sameCBMCount\":" + sameCBMCount
//                            + ", \"silent\":" + thisFrame.silentedTagList.size()
                            + "}\n"
            );
            //更新帧
            lastFrame = thisFrame;
        }
        fileWriter.writemsg("]\n");


        return resList;
    }


    /**
     * 计算两个帧的CBMs中相同的唯一碰撞集的个数
     *
     * @param CBMs1 当前帧的CBMs
     * @param CBMs2 上一帧的CBMs
     * @return 两个帧的唯一碰撞集相等的个数
     */
    private int figureSameCBMCount(List<Map<String, Set<String>>> CBMs1, List<Map<String, Set<String>>> CBMs2) {
        int r = 0;
        Set<Set<String>> s1 = new HashSet<>();
        for (Map<String, Set<String>> m1 : CBMs1) {
            Set<String> s = new HashSet<>();
            for (Set<String> ss : m1.values()) {
                s.addAll(ss);
            }
            s1.add(s);
        }
//        fileWriterFeng.writemsg("thisFrame:[\n");
//        for (Set<String> ss: s1){
//            fileWriterFeng.writemsg(""+ss+"\n");
//        }
//        fileWriterFeng.writemsg("]\n");
        Set<Set<String>> s2 = new HashSet<>();
        for (Map<String, Set<String>> m : CBMs2) {
            Set<String> s = new HashSet<>();
            for (Set<String> ss : m.values()) {
                s.addAll(ss);
            }
            s2.add(s);
        }
//        fileWriterFeng.writemsg("lastFrame:[\n");
//        for (Set<String> ss: s2){
//            fileWriterFeng.writemsg(""+ss+"\n");
//        }
//        fileWriterFeng.writemsg("]\n");
        s1.retainAll(s2);
        r = s1.size();
//        fileWriterFeng.writemsg("theSame:[\n");
//        for (Set<String> ss: s1){
//            fileWriterFeng.writemsg(""+ss+"\n");
//        }
//        fileWriterFeng.writemsg("]\n");
        fileWriterFeng.writemsg("theSameCount:" + r + "\n");
        return r;

//        int[] r = new int[]{0,0};
//        fileWriterFeng.writemsg("thisFrame:[\n");
//        for (Map<String, Set<String>> cbm1: CBMs1){
//            fileWriterFeng.writemsg(","+cbm1+"\n");
//        }
//        fileWriterFeng.writemsg("]\n");
//        fileWriterFeng.writemsg("lastFrame:[\n");
//        for (Map<String, Set<String>> cbm2: CBMs2){
//            fileWriterFeng.writemsg(","+cbm2+"\n");
//        }
//        fileWriterFeng.writemsg("]\n");
//        fileWriterFeng.writemsg("thesame:[\n");
//        for (Map<String, Set<String>> cbm1: CBMs1){
//            for (Map<String, Set<String>> cbm2: CBMs2){
//                Set<String> collisionBits1 = cbm1.keySet();
//                Set<String> collisionBits2 = cbm2.keySet();
//                if (collisionBits1 != null && collisionBits1.equals(collisionBits2)){
//                    r[0]++;
//                    fileWriterFeng.writemsg(""+cbm1+"\n");
//                    boolean f = true;
//                    for (String s: collisionBits1){
//                        if (!cbm1.get(s).equals(cbm2.get(s))) {
//                            f = false;
//                            break;
//                        }
//                    }
//                    if (f) r[1]++;
//                }
//            }
//        }
//        fileWriterFeng.writemsg("]\n");
//        fileWriterFeng.writemsg("theSameTotal:"+r[0]+"\n");
//        return r;

    }


    /**
     * 将两个字符串进行碰撞，如果字符不一样，设为X，如果有NS，返回另一个字符串
     *
     * @param s1 参与碰撞的字符串
     * @param s2 参与碰撞的字符串
     * @return 碰撞结果。如果字符不一样，结果相应位置设为X；如果字符串有任一个为NS，返回另一个字符串
     */
    private String CB(String s1, String s2) {
        String result;
        if (s1.equals("NS")) {
            result = s2;
        } else if (s2.equals("NS")) {
            result = s1;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s1.length(); i++) {
                if (s1.charAt(i) == s2.charAt(i)) {
                    sb.append(s1.charAt(i));
                } else {
                    sb.append('X');
                }
            }
            result = sb.toString();
        }
        return result;
    }

    // 打印CCB和PCB
    private void printCCBandPCB(String name, Map<String, Set<String>> input) {
        String result = "";
        if (name.equals("CCB")) {
            result += "CCB: {";
        } else {
            result += "PCB: {";
        }
        for (String key : input.keySet()) {
            String local = "";
            for (String value : input.get(key)) {
                local += value + " ";
            }
            result += "[" + key + ", {" + local + "}" + "], ";
        }
        result += "}";
        logger.info(result);
    }

    // 打印CUCS，如果CUCS为空则打印“{}”
    private void printCUCS(Set<Integer> input) {
        String result = "CUCS: {";
        if (input.isEmpty()) {
            result += "}";
            logger.info(result);
            return;
        }
        for (int i : input) {
            result += i + ", ";
        }
        result += "}";
        logger.info(result);
    }


    /**
     * 模拟读写器到标签的广播
     */
    class slot {
        public String broadcast;
        public String receive;
        public List<String> store = new ArrayList<>();

        public slot() {
            broadcast = "";
            receive = "";
        }

        public String getSlotInfo() {
            return "the broadcast is " + broadcast + ",the receive is " + receive;
        }

        public void add(String s) {
            store.add(s);
        }

        public void addReceive() {
            for (String s : store) {
                if (receive == "") {
                    receive = s;
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < s.length(); i++) {
                        if (s.charAt(i) == receive.charAt(i)) sb.append(s.charAt(i));
                        else sb.append('X');
                    }
                    receive = sb.toString();
                }
            }
            if (receive.contains("X")) {
                // 添加捕获效应，概率为captureError%
                int w = new Random().nextInt(100);
                if (w < captureError) {
                    receive = store.get(0);
                }
            }
        }

        public void clear() {
            this.receive = "";
            this.broadcast = "";
            this.store.clear();
        }
    }
}
