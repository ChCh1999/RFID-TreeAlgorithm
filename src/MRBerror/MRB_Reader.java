package MRBerror;

import base.Tag;
import org.apache.log4j.Logger;

import java.util.*;

import static MRBerror.MRB_Main.*;

class DataRecord {
    double p = 0;//估计标签丢失概率p
    double n = 0;//估计的标签数目n
    double pm = 0;//丢失任一个标签的概率
    int slotCount = 0;//本轮使用的时隙数
    int countOfCatchedTag = 0;

    @Override
    public String toString() {
        return "{" +
                "\"p\":" + p +
                ", \"n\":" + n +
                ", \"pm\":" + pm +
                ", \"slotCount\":" + slotCount +
                ", \"countOfCatchedTag\":" + countOfCatchedTag +
                "}\n";
    }
}

public class MRB_Reader {
    protected static Logger logger = Logger.getLogger(MRB_Reader.class);
    protected static Logger datalogger = Logger.getLogger("MRBRecord");
    /*
     * 往帧识别的标签集合
     */
    public List<MRB_Tag> LastFrameTagList;

    public slot slot;
    public Set<MRB_Tag> catchedTagSet;

    public MRB_Reader() {
        LastFrameTagList = new ArrayList<>();
        slot = new slot();
        catchedTagSet = new HashSet<>();
    }

    public void clearCatchedSet() {
        catchedTagSet = new HashSet<>();
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
        int countOfSlot;

        public resu() {
            identifiedTagList = null;
            silentedTagList = null;
            countOfSlot = 0;
        }

        @Override
        public String toString() {
            return "resu{" +
                    "identifiedTagList=" + identifiedTagList +
                    ", silentedTagList=" + silentedTagList +
                    ", countOfSlot=" + countOfSlot +
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
                double p4 = (double) r.countOfSlot;
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
                    p1 = (double) (c * n1.p1 + p1) / (double) (c + 1);
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


    // 仲裁层入口
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
                if (CCB.keySet().contains(CB(s, SN_i))) {
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

                slot.broadcast = String.valueOf(i - groupSize) + "|" + String.valueOf(groupSize);

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

        slot.broadcast = String.valueOf(i - groupSize) + "|" + String.valueOf(groupSize);

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
     * 重载识别过程 指定沉默策略,添加沉默标签
     *
     * @param l               标签集合
     * @param silenceStrategy 沉默策略序号 1.随机沉默
     * @return resu(包括识别的标签 、 沉默的标签 、 识别用的时隙数)
     */
    /*public resu OneFrame(List<MRB_Tag> l, int silenceStrategy) {
        // 这一帧已识别标签ID集合
        List<String> currentFrameTagList = new ArrayList<>();
//        // 这一帧待识别标签集合
//        List<MRB_Tag> l = new ArrayList<>();
        // 这一帧未发生静态错误的标签集合，用于标签传输阶段
        List<MRB_Tag> TheTrueTag = new ArrayList<>();
        //存储唯一碰撞集
        List<Map<String, Set<String>>> CBMs = new ArrayList<>();
        // 结果反馈
        resu res = new resu();


        // 初始化MRBTagList标签列表，之前帧中设定沉默的标签被筛除
//         这儿是修改部分，修改了这儿导致运行卡住。需要检查是为什么
//        for (MRB_Tag t : l) {
//            if (t.use) {
//                MRBTagList.add(t);
//            }
//        }



        // 生成0-100的随机数,当大于等于20时存入NowTag，模拟20%概率在本次读写器周期内丢失标签（静态错误）
        for (MRB_Tag t : l) {
            int a = new Random().nextInt(100);
            if (a >= staticError) {
                TheTrueTag.add(t);
            }
        }
       // 此处的标签ID长度即为论文中SN长度
        int tagIDLength = l.get(0).ID.length();

        logger.info("----------帧开始----------");
//        System.out.println("帧开始");
        // 初始化输出信息
        int slotNum = 0;
        int totalBitNum = 0;
        double totalTime = 0.0;

        // CUCS是ASC的集合
        Set<Integer> CUCS = new HashSet<>();
        Map<String, Set<String>> PCB = new HashMap<>();
        Map<String, Set<String>> CCB = new HashMap<>();

        // 初始化CCB
        String firstID = l.get(0).ID;
        Set<String> CCBValue = new HashSet<>();
        CCBValue.add(firstID);
        CCB.put(firstID, CCBValue);
        // 初始化PCB
        PCB.put("NS", new HashSet<>());
        Set<String> silentedTagIDSet = new HashSet<>();
        int i = 0;
        int N = l.size();
        int d = 0; // 用于统计跳过的被沉默的标签的个数，这些被沉默标签数目应当在通信时加入到groupsize中，并重置
        while (i < N) {
            // 如果遇到被沉默（未被复用的标签），则分析时跳过该标签
            // 注意到通信时依然会发消息给包含该标签的集合，此时该标签不接受响应
            if (!l.get(i).use) {
                silentedTagIDSet.add(l.get(i).ID);
                i++;
                d++;
                continue;
            }
            logger.info("----------增加标签" + l.get(i).ID + "----------");
            logger.info("i = " + i);
            logger.info("-----更新CCB-----");
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
                } else {
                    CCB.put(CB(s, SN_i), tagsClone);
                }
            }
            printCCBandPCB("CCB", CCB);

            for (String s1 : CCB.keySet()) {
                for (String s2 : PCB.keySet()) {
                    if (s1.equals(s2)) {
                        hasDumplicate = true;
                        break;
                    }
                }
            }

            if (hasDumplicate) {
                slotNum++;
                logger.info("-----CCB和PCB有重复，不再增加标签-----");
                int groupSize = CUCS.size();
                // 加入被跳过的标签，使得响应列表连续
                groupSize += d;
                d = 0;
                Map<String, Set<String>> CCBcopy = new HashMap<>(CCB);
                CBMs.add(CCBcopy);
                CUCS.clear();
                CCB.clear();
                // 在PCB被重置之前根据PCB完成操作
                logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
                logger.info("---------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID-----------------");

                slot.broadcast = String.valueOf(i - groupSize) + "|" + String.valueOf(groupSize);

                // 标签接受广播并进行响应
                MRB_Transmission.Broadcast(this, TheTrueTag);
                // 读取器接受标签响应
                MRB_Transmission.receiveReplies(this, TheTrueTag);
//                System.out.println("receive msg from tag: " + this.slot.receive);
                logger.info("receive msg from tag: " + this.slot.receive);
                int num = 0;   // 统计识别的标签数目
                String s2 = "";
                List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
                // 读取器处理标签响应，并将识别得到的标签存入已识别列表
                for (String s : PCB.keySet()) {
                    if (s.equals(this.slot.receive)) {
                        logger.info("识别标签" + PCB.get(s));
//                        System.out.println("识别标签" + PCB.get(s));
                        for (String st : PCB.get(s)) {
                            currentFrameTagList.add(st);
                            num++;
                            s2 = st;
                        }
                    }
                }

                this.slot.clear();


                PCB.clear();
                // 初始化CCB
                String currentID = l.get(i).ID;
                Set<String> CurrCCBValue = new HashSet<>();
                CCBValue.add(currentID);
                CCB.put(currentID, CurrCCBValue);

                // 初始化PCB
                PCB.put("NS", new HashSet<>());
                logger.info("-----重置状态-----");
                printCUCS(CUCS);
                printCCBandPCB("CCB", CCB);
                printCCBandPCB("PCB", PCB);
                logger.info("");

            } else {
                logger.info("-----CCB和PCB没重复，增加标签-----");
                PCB.putAll(CCB);
                CUCS.add(i);
                i++;
                logger.info("-----结束时状态-----");
                printCUCS(CUCS);
                printCCBandPCB("CCB", CCB);
                printCCBandPCB("PCB", PCB);
            }


        }

        logger.info("----------最后一个标签----------");
        int groupSize = CUCS.size();
        groupSize += d;
        //拷贝CCB到列表
        Map<String, Set<String>> CCBcopy = new HashMap<>(CCB);
        CBMs.add(CCBcopy);
        logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
        logger.info("----------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID------------------");

        slot.broadcast = String.valueOf(i - groupSize) + "|" + String.valueOf(groupSize);

        // 标签接受广播并进行响应
        MRB_Transmission.Broadcast(this, TheTrueTag);
        // 读取器接受标签响应
        MRB_Transmission.receiveReplies(this, TheTrueTag);

//        System.out.println("receive msg from tag: " + this.slot.receive);
        logger.info("receive msg from tag: " + this.slot.receive);
        int num = 0;   // 统计识别的标签数目
        String s2 = "";
        List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
        // 读取器处理标签响应，并将识别得到的标签存入已识别列表
        for (String s : PCB.keySet()) {
            if (s.equals(this.slot.receive)) {
                logger.info("识别标签" + PCB.get(s));
//                System.out.println("识别标签" + PCB.get(s));
                for (String st : PCB.get(s)) {
                    currentFrameTagList.add(st);
                    num++;
                    s2 = st;
                }
            }
        }

        this.slot.clear();

        //记录识别的标签
        // 将string换成对应的MRB_Tag
        List<MRB_Tag> result = new ArrayList<>();
        for (MRB_Tag t : l) {
            if (currentFrameTagList.contains(t.ID)) {
                result.add(t);
                catchedTagSet.add(t);
            }
        }
        //根据参数对标签进行沉默
        int countOfCatchedTag = currentFrameTagList.size();         //捕获的标签数
        int silentCount = (100 - thev) * catchedTagSet.size() / 100 - silentedTagIDSet.size();            //至少需要沉默的标签数
        List<Integer> silentedIndex = new ArrayList<Integer>();        //沉默的序号，在currentFrameTagList中定位
        List<String> silentIDs = new ArrayList<String>();            //将要沉默的标签的ID集合


        if(currentFrameTagList.size()<silentCount)silenceStrategy = -1;//若捕获数不满足最小需求，沉默所有捕获的标签
        if(silentCount<0)silenceStrategy=-2;
        List<MRB_Tag> toSilenteTagList = new ArrayList<>();

        switch (silenceStrategy) {
            default:
                break;
            case -1:
                silentIDs.addAll(currentFrameTagList);
                break;
            case 0:
                //随机沉默
                int tagCount=currentFrameTagList.size();
                Random random = new Random();
                //随机选择沉默的标签序号
                for (int silentIndex = 0; silentIndex < silentCount; silentIndex++) {
                    //随机生成沉默序号
                    int index = -1;
                    do {
                        index = random.nextInt(tagCount);
                    } while (silentedIndex.contains(index));
                    silentedIndex.add(index);
                    //添加index的ID
                    silentIDs.add(currentFrameTagList.get(index));
                }
                break;
            case 2:
                //从小的CBM沉默
                CBMs.sort(Comparator.comparing(Map::size));
            case 1:

                //以CBM为单位沉默
                for (Map<String, Set<String>> cbm : CBMs) {
                    if (silentIDs.size() < silentCount) {
                        //将cbm的所有相关标签添加到沉默列表
                        for (Set<String> id_list : cbm.values()) {
                            for (String id : id_list) {
                                if (!silentIDs.contains(id)) {
                                    silentIDs.add(id);
                                }
                            }
                        }
                    }
                }
                break;
            case 3:
                //精确沉默
                int cbm_index = 0;
                //循环直到找齐
                while (silentIDs.size() < silentCount) {
                    //每次取出一个CBM中的第一个
                    for (Set<String> id_list : CBMs.get(cbm_index).values()) {
                        boolean find = false;
                        for (String id : id_list) {
                            if (!silentIDs.contains(id)) {
                                silentIDs.add(id);
                                find = true;
                                break;
                            }
                        }
                        if (find) break;
                    }
                    cbm_index = (cbm_index + 1) % CBMs.size();
                }
                break;
        }

        //沉默相应标签
        for (MRB_Tag tag : l) {
            if (silentIDs.contains(tag.ID)) {
                tag.use = false;
                logger.info("沉默标签" + tag.ID);
                toSilenteTagList.add(tag);
            }
        }

        slotNum++;

        logger.info("---------这轮识别结束，共消耗" + slotNum + "个时隙");
        logger.info("---------已识别标签列表如下：");
        for (String s : currentFrameTagList) {
            logger.info(s);
        }

        res.identifiedTagList = result;
        res.silentedTagList = toSilenteTagList;
        res.countOfSlot = slotNum;
        return res;
    }*/


    /**
     * 通过多轮识别进行估计
     *
     * @param mrb_tags        参与识别的标签集合
     * @param silenceStrategy 沉默策略
     * @param thresholdOfPM   pm的阈值
     * @return 估计结果
     *//*
    public List<DataRecord> MultiSession(List<MRB_Tag> mrb_tags, int silenceStrategy, double thresholdOfPM) {
//        double p_true=
        double pm = 1;
        ArrayList<DataRecord> resList = new ArrayList<>(); //记录结果
        int R = 0;//R记录循环轮次
        int totalSilentedCount = 0;
        resu lastFrame = OneFrame(mrb_tags, silenceStrategy);
        totalSilentedCount = lastFrame.silentedTagList.size();
        while (pm > thresholdOfPM && R < 20) {

            //TODO:从已识别的全部标签中沉默

            resu thisFrame = OneFrame(mrb_tags, silenceStrategy);

            //统计数据
            int m = 0;    // 两次均找到的标签数  对应m
            //计数 CommonTagCount
            for (MRB_Tag tag : thisFrame.identifiedTagList) {
                if (lastFrame.identifiedTagList.contains(tag)) m++;
            }
            //沉默标签数 k1
            int k1 = lastFrame.silentedTagList.size();
            //重用但没找到的标签数 l=上轮识别-上轮沉默-两轮均识别
            int l = lastFrame.identifiedTagList.size() - k1 - m;
            //上轮未识别这轮新识别的标签 k2
            int k2 = thisFrame.identifiedTagList.size() - m;

            //估计结果
            //错误概率p的估计值 p=l/l+m
            double p = (double) l / (l + m);
            //p与之前结果取均值
            for (DataRecord dataRecord : resList) p += dataRecord.p;
            p = p / (resList.size() + 1);
            //参与识别的标签数N的估计值
//            double N = ((double) (k1 + k2 + l + m))
//                    / ((double) 1 - Math.pow(p, 2));
            double N=((double) (lastFrame.identifiedTagList.size()+thisFrame.identifiedTagList.size()-m))
                    / ((double) 1 - Math.pow(p, 2));
            //pm的估计值
            pm = 1 - Math.pow(1 - Math.pow(p, R+1), (int) N);

            //保存结果
            DataRecord res = new DataRecord();
            //估计标签数
            res.n = N;
            //实际标签数
            res.n_true = mrb_tags.size() - totalSilentedCount;
            //更新沉默标签数
            totalSilentedCount += thisFrame.silentedTagList.size();
            //估计丢失标签概率
            res.p = p;
            //时隙数
            res.slotCount = thisFrame.countOfSlot;
            //pm
            res.pm = pm;
            resList.add(res);
            R++;

            //更新帧
            lastFrame = thisFrame;
        }
        return resList;
    }
    */

    /**
     * 重载识别过程 指定沉默策略,重置沉默标签
     *
     * @param l               标签集合
     * @param silenceStrategy 沉默策略序号 1.随机沉默
     * @return resu(包括识别的标签 、 沉默的标签 、 识别用的时隙数)
     */
    public resu OneFrame(List<MRB_Tag> l, int silenceStrategy) {
        // 这一帧已识别标签ID集合
        List<String> currentFrameTagList = new ArrayList<>();
//        // 这一帧待识别标签集合
//        List<MRB_Tag> l = new ArrayList<>();
        // 这一帧未发生静态错误的标签集合，用于标签传输阶段
        List<MRB_Tag> TheTrueTag = new ArrayList<>();
        //存储唯一碰撞集
        List<Map<String, Set<String>>> CBMs = new ArrayList<>();
        // 结果反馈
        resu res = new resu();

/*
        // 初始化MRBTagList标签列表，之前帧中设定沉默的标签被筛除
//         这儿是修改部分，修改了这儿导致运行卡住。需要检查是为什么
        for (MRB_Tag t : l) {
            if (t.use) {
                MRBTagList.add(t);
            }
        }
*/


        // 生成0-100的随机数,当大于等于20时存入NowTag，模拟20%概率在本次读写器周期内丢失标签（静态错误）
        for (MRB_Tag t : l) {
            int a = new Random().nextInt(100);
            if (a >= staticError) {
                TheTrueTag.add(t);
            }
        }

/*        // 此处的标签ID长度即为论文中SN长度
        int tagIDLength = MRBTagList.get(0).ID.length();*/

        logger.info("----------帧开始----------");
//        System.out.println("帧开始");
        // 初始化输出信息
        int slotNum = 0;
        int totalBitNum = 0;
        double totalTime = 0.0;

        // CUCS是ASC的集合
        Set<Integer> CUCS = new HashSet<>();
        Map<String, Set<String>> PCB = new HashMap<>();
        Map<String, Set<String>> CCB = new HashMap<>();

        // 初始化CCB
        String firstID = l.get(0).ID;
        Set<String> CCBValue = new HashSet<>();
        CCBValue.add(firstID);
        CCB.put(firstID, CCBValue);
        // 初始化PCB
        PCB.put("NS", new HashSet<>());
        List<MRB_Tag> silentedTagIDList = new ArrayList<>();
        int i = 0;
        int N = l.size();
        int d = 0; // 用于统计跳过的被沉默的标签的个数，这些被沉默标签数目应当在通信时加入到groupsize中，并重置
        while (i < N) {
            // 如果遇到被沉默（未被复用的标签），则分析时跳过该标签
            // 注意到通信时依然会发消息给包含该标签的集合，此时该标签不接受响应
            if (!l.get(i).use) {
                silentedTagIDList.add(l.get(i));
                i++;
                d++;
                continue;
            }
            logger.info("----------增加标签" + l.get(i).ID + "----------");
            logger.info("i = " + i);
            logger.info("-----更新CCB-----");
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
                } else {
                    CCB.put(CB(s, SN_i), tagsClone);
                }
            }
            printCCBandPCB("CCB", CCB);

            for (String s1 : CCB.keySet()) {
                for (String s2 : PCB.keySet()) {
                    if (s1.equals(s2)) {
                        hasDumplicate = true;
                        break;
                    }
                }
            }

            if (hasDumplicate) {
                slotNum++;
                logger.info("-----CCB和PCB有重复，不再增加标签-----");
                int groupSize = CUCS.size();
                // 加入被跳过的标签，使得响应列表连续
                groupSize += d;
                d = 0;
                Map<String, Set<String>> CCBcopy = new HashMap<>(CCB);
                CBMs.add(CCBcopy);
                CUCS.clear();
                CCB.clear();
                // 在PCB被重置之前根据PCB完成操作
                logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
                logger.info("---------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID-----------------");

                slot.broadcast = String.valueOf(i - groupSize) + "|" + String.valueOf(groupSize);

                // 标签接受广播并进行响应
                MRB_Transmission.Broadcast(this, TheTrueTag);
                // 读取器接受标签响应
                MRB_Transmission.receiveReplies(this, TheTrueTag);
//                System.out.println("receive msg from tag: " + this.slot.receive);
                logger.info("receive msg from tag: " + this.slot.receive);
                int num = 0;   // 统计识别的标签数目
                String s2 = "";
                List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
                // 读取器处理标签响应，并将识别得到的标签存入已识别列表
                for (String s : PCB.keySet()) {
                    if (s.equals(this.slot.receive)) {
                        logger.info("识别标签" + PCB.get(s));
//                        System.out.println("识别标签" + PCB.get(s));
                        for (String st : PCB.get(s)) {
                            currentFrameTagList.add(st);
                            num++;
                            s2 = st;
                        }
                    }
                }

                this.slot.clear();


                PCB.clear();
                // 初始化CCB
                String currentID = l.get(i).ID;
                Set<String> CurrCCBValue = new HashSet<>();
                CCBValue.add(currentID);
                CCB.put(currentID, CurrCCBValue);

                // 初始化PCB
                PCB.put("NS", new HashSet<>());
                logger.info("-----重置状态-----");
                printCUCS(CUCS);
                printCCBandPCB("CCB", CCB);
                printCCBandPCB("PCB", PCB);
                logger.info("");

            } else {
                logger.info("-----CCB和PCB没重复，增加标签-----");
                PCB.putAll(CCB);
                CUCS.add(i);
                i++;
                logger.info("-----结束时状态-----");
                printCUCS(CUCS);
                printCCBandPCB("CCB", CCB);
                printCCBandPCB("PCB", PCB);
            }


        }

        logger.info("----------最后一个标签----------");
        int groupSize = CUCS.size();
        groupSize += d;
        //拷贝CCB到列表
        Map<String, Set<String>> CCBcopy = new HashMap<>(CCB);
        CBMs.add(CCBcopy);
        logger.info("-----广播" + (i - groupSize) + "和" + groupSize + "-----");
        logger.info("----------------标签的ASC在[" + (i - groupSize) + "和" + i + ") 之间的标签发送ID------------------");

        slot.broadcast = String.valueOf(i - groupSize) + "|" + String.valueOf(groupSize);

        // 标签接受广播并进行响应
        MRB_Transmission.Broadcast(this, TheTrueTag);
        // 读取器接受标签响应
        MRB_Transmission.receiveReplies(this, TheTrueTag);

//        System.out.println("receive msg from tag: " + this.slot.receive);
        logger.info("receive msg from tag: " + this.slot.receive);
        int num = 0;   // 统计识别的标签数目
        String s2 = "";
        List<MRB_Tag> t3 = new ArrayList<MRB_Tag>();
        // 读取器处理标签响应，并将识别得到的标签存入已识别列表
        for (String s : PCB.keySet()) {
            if (s.equals(this.slot.receive)) {
                logger.info("识别标签" + PCB.get(s));
//                System.out.println("识别标签" + PCB.get(s));
                for (String st : PCB.get(s)) {
                    currentFrameTagList.add(st);
                    num++;
                    s2 = st;
                }
            }
        }

        this.slot.clear();

        //记录识别的标签
        // 将string换成对应的MRB_Tag
        List<MRB_Tag> result = new ArrayList<>();
        for (MRB_Tag t : l) {
            if (currentFrameTagList.contains(t.ID)) {
                result.add(t);
                catchedTagSet.add(t);
            }
        }


        //根据参数对标签进行沉默
        int countOfCatchedTag = currentFrameTagList.size();         //捕获的标签数
        int silentCount = (100 - thev) * catchedTagSet.size() / 100;            //至少需要沉默的标签数
        List<Integer> silentedIndex = new ArrayList<Integer>();        //沉默的序号，在currentFrameTagList中定位
        List<String> silentIDs = new ArrayList<String>();            //将要沉默的标签的ID集合


        List<MRB_Tag> toSilenteTagList = new ArrayList<>();
        List<MRB_Tag> catchedTagList = new ArrayList<>(catchedTagSet);
        switch (silenceStrategy) {
            default:
            case -1:
//                silentIDs.addAll(currentFrameTagList);
                break;
            case 0:
                //随机沉默

                //重置已沉默标签
                for (MRB_Tag tag : silentedTagIDList) tag.use = true;
                silentedTagIDList = new ArrayList<>();

                int tagCount = catchedTagList.size();
                Random random = new Random();
                //随机选择沉默的标签序号
                for (int silentIndex = 0; silentIndex < silentCount; silentIndex++) {
                    //随机生成沉默序号
                    int index = -1;
                    do {
                        index = random.nextInt(tagCount);
                    } while (silentedIndex.contains(index));
                    silentedIndex.add(index);
                    //添加index的ID
                    silentIDs.add(catchedTagList.get(index).ID);
                }
                break;
            case 2:
                //从小的CBM沉默
                CBMs.sort(Comparator.comparing(Map::size));
            case 1:
                //以CBM为单位沉默
                //添加已沉默标签
                silentCount = silentCount - silentedTagIDList.size();
                if (silentCount <= 0) break;

                for (Map<String, Set<String>> cbm : CBMs) {
                    if (silentIDs.size() < silentCount) {
                        //将cbm的所有相关标签添加到沉默列表
                        for (Set<String> id_list : cbm.values()) {
                            for (String id : id_list) {
                                if (!silentIDs.contains(id)) {
                                    silentIDs.add(id);
                                }
                            }
                        }
                    }
                }
                break;
            case 3:
                //精确沉默
                //添加已沉默标签
                silentCount = silentCount - silentedTagIDList.size();
                if (silentCount <= 0) break;

                int cbm_index = 1;
                //循环直到找齐
                while (silentIDs.size() < silentCount) {
                    //每次取出一个CBM中的第一个
                    for (Set<String> id_list : CBMs.get(cbm_index).values()) {
                        boolean find = false;
                        for (String id : id_list) {
                            if (!silentIDs.contains(id)) {
                                silentIDs.add(id);
                                find = true;
                                break;
                            }
                        }
                        if (find) break;
                    }
                    cbm_index = (cbm_index + 1) % CBMs.size();
                }
                break;
            case 4:
                //随机沉默-递增方案

                silentCount = silentCount - silentedTagIDList.size();
                if (silentCount <= 0) break;


                tagCount = catchedTagList.size();
                random = new Random();
                //随机选择沉默的标签序号
                for (int silentIndex = 0; silentIndex < silentCount; silentIndex++) {
                    //随机生成沉默序号
                    int index = -1;
                    do {
                        index = random.nextInt(tagCount);
                    } while (silentedIndex.contains(index));
                    silentedIndex.add(index);
                    //添加index的ID
                    silentIDs.add(catchedTagList.get(index).ID);
                }
                break;
            case 6:
                //从小的CBM沉默
                CBMs.sort(Comparator.comparing(Map::size));
            case 5:
                //以CBM为单位沉默
                //重置已沉默标签
                for (MRB_Tag tag : silentedTagIDList) tag.use = true;
                silentedTagIDList = new ArrayList<>();

                for (Map<String, Set<String>> cbm : CBMs) {
                    if (silentIDs.size() < silentCount) {
                        //将cbm的所有相关标签添加到沉默列表
                        for (Set<String> id_list : cbm.values()) {
                            for (String id : id_list) {
                                if (!silentIDs.contains(id)) {
                                    silentIDs.add(id);
                                }
                            }
                        }
                    }
                }
                break;
        }

        //沉默相应标签
        for (MRB_Tag tag : l) {
            if (!tag.use) {
                toSilenteTagList.add(tag);
                continue;
            }
            if (silentIDs.contains(tag.ID) && catchedTagSet.contains(tag)) {
                tag.use = false;
                logger.info("沉默标签" + tag.ID);
                toSilenteTagList.add(tag);
            }
        }

        slotNum++;

        logger.info("---------这轮识别结束，共消耗" + slotNum + "个时隙");
        logger.info("---------已识别标签列表如下：");
        for (String s : currentFrameTagList) {
            logger.info(s);
        }

        res.identifiedTagList = result;
        res.silentedTagList = toSilenteTagList;
        res.countOfSlot = slotNum;
        return res;
    }

    /**
     * 通过两轮识别估计错误概率
     *
     * @param l
     * @param silenceStrategy
     * @return
     */
    public NUM getErrorProbablity(List<MRB_Tag> l, int silenceStrategy) {
        NUM result = new NUM();
        MRB_Reader.resu res = OneFrame(l, silenceStrategy);
        MRB_Reader.resu res2 = OneFrame(l, silenceStrategy);
        int missFrame2 = 0, catchedFrame2 = 0;
        List<MRB_Tag> reuseTagList = new ArrayList<>(res.identifiedTagList);
        for (MRB_Tag tag : res.silentedTagList) {
            reuseTagList.remove(tag);
        }
        for (MRB_Tag tag : reuseTagList) {
            if (res2.identifiedTagList.contains(tag)) catchedFrame2++;
            else missFrame2++;
        }
//        System.out.println("第二次未捕获的标签"+missFrame2);
//        System.out.println("第二次重用的标签"+reuseTagList.size());
        result.slotCount = res.countOfSlot;
        if (reuseTagList.size() == 0)
            result.p4 = 1;
        else
            result.p4 = (double) missFrame2 / reuseTagList.size();
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

        double pm = 1;
        clearCatchedSet();  //重置已捕获的标签记录
        ArrayList<DataRecord> resList = new ArrayList<>(); //记录结果
        int R = 0;//R记录循环轮次
        //重置标签
        for (MRB_Tag tag : mrb_tags) tag.use = true;
        //第0轮识别
        int totalSilentedCount = 0;
        resu lastFrame = OneFrame(mrb_tags, silenceStrategy);
        //保存结果
        DataRecord res = new DataRecord();


        //pm
        res.pm = pm;
        //总的识别标签数
        res.countOfCatchedTag = catchedTagSet.size();
        resList.add(res);

        datalogger.info(",[");
        while (pm > thresholdOfPM && R < 20) {

            resu thisFrame = OneFrame(mrb_tags, silenceStrategy);

            //统计数据
            int m = 0;    // 两次均找到的标签数  对应m
            //沉默标签数 k1
            int k1 = 0;
            //重用但没找到的标签数 l=上轮识别-上轮沉默-两轮均识别
            int l = 0;
            //上轮未识别这轮新识别的标签 k2
            int k2 = 0;
            //计数 CommonTagCount
            for (MRB_Tag tag : catchedTagSet) {
                if (lastFrame.silentedTagList.contains(tag)) k1++;  //上轮被沉默的标签
                    //参与本次识别的标签
                else if (!thisFrame.identifiedTagList.contains(tag)) l++;//本轮未被识别的标签
                    //本轮被识别的标签
                else m++;//本轮被识别的标签
            }
            k2 = catchedTagSet.size() - resList.get(resList.size() - 1).countOfCatchedTag;
            m = m - k2;

            //估计结果
            //错误概率p的估计值 p=l/l+m
            double p = (double) l / (l + m);
            //p与之前结果取均值
            for (DataRecord dataRecord : resList) p += dataRecord.p;
            p = p / (resList.size());
            //参与识别的标签数N的估计值
//            System.out.println(catchedTagSet.size());
            double N = ((double) (k1 + k2 + l + m))
                    / ((double) 1 - Math.pow(p, R + 1));
            //pm的估计值
            pm = 1 - Math.pow(1 - Math.pow(p, R + 1), (int) N);


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
            res.countOfCatchedTag = catchedTagSet.size();
            resList.add(res);
            R++;

            //写入json格式日志
            if (R != 1) {
                datalogger.info(",");
            }
            datalogger.info(
                    "{" +
                            "\"k1\":" + k1
                            + ", \"k2\":" + k2
                            + ", \"l\":" + l
                            + ", \"m\":" + m
                            + ", \"p\":" + p
                            + ", \"n\":" + N
                            + ", \"pm\":" + pm
                            + ", \"catched\":" + catchedTagSet.size()
                            + ", \"slot\":" + thisFrame.countOfSlot
//                            + ", \"silent\":" + thisFrame.silentedTagList.size()
                            + "}"
            );
            //更新帧
            lastFrame = thisFrame;
        }
        datalogger.info("]");
        return resList;
    }

    // 将两个字符串进行碰撞，如果字符不一样，设为X，如果有NS，返回另一个字符串
    private String CB(String s1, String s2) {
        if (s1.equals("NS")) return s2;
        if (s2.equals("NS")) return s1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) == s2.charAt(i)) sb.append(s1.charAt(i));
            else sb.append('X');
        }
        return sb.toString();
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
