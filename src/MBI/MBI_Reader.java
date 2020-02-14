package MBI;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MBI_Reader {
    private static Logger logger = Logger.getLogger(MBI_Reader.class);
    // 已识别标签集合
    public List<String> tagIDList = new ArrayList<>();
    // the stack to preserve the prefixes.存储前缀列表的堆栈
    public Stack<String> stack = new Stack<>();
    // the store of the common strings of DGS(L).用于存储公共字符串的list，其与每个标签中的CS_Memory保持一致，在识别过程之前完成
    public List<String> CS_Memory;
    // 识别块长度(MBI识别模式为逐块识别)
    public int L;
    // 前缀
    public String prefix;

    // 构造函数
    public MBI_Reader(int L,List CS_Memory){
        this.L = L;
        this.CS_Memory = CS_Memory;
        prefix = "";
    }

    public MBI_Output Distinguish(MBI_Input input){
        logger.info("---------识别开始-----------");
        logger.info("---------待识别的所有标签ID----------");
        for(MBI_Tag t:input.MBITagList){
            logger.info(t.toString());
        }
        logger.info("--------CS_Memory的内容-------");
        for(String s:CS_Memory){
            logger.info(s);
        }
        int theWholeTagLength = input.MBITagList.get(0).ID.length();            //  标签的整个ID的长度

        // 初始化输出信息
        int slotNum = 0;
        int totalBitNum = 0;
        double totalTime = 0.0;

        int theM_SlotNum = 0;
        int theL = 0;

        do{      // 开始循环
            // 进入B-Slot，对每个标签的slot类进行预处理
            if(theL == 0){
                logger.info("-------进入B-Slot---------");
                logger.info("-------prefix为" + prefix + "---------");
                logger.info("-------stack内的前缀为---------");
                for(String s: stack){
                    logger.info(s);
                }
            }
            int theTagLength = 0;           //  该B-slot中标签传输的长度
            String s_store = "";            //  储存如果该B-slot未发生碰撞，此时的标签传输的数据内容
            String s2_store = "";           //  存储当发生一次碰撞时，当前碰撞点之前的信息
            logger.info("--------发生响应的标签的接受信息与传输信息---------");
            for(MBI_Tag t:input.MBITagList){
                t.slot.tagID ="";
                t.slot.feedback = "0" + prefix;
                t.transmitID();
                logger.info("-----feedback: " + t.slot.feedback + "  tagID: " + t.slot.tagID + "------");
                if(!t.slot.tagID.equals("")){
                    theTagLength = t.slot.tagID.length();
                }
            }
            // 计算该次查询中，传输比特数
            if(theL == 0){       // 如果当前传输为代码模拟而实际未发生，则不计入（即theL==1的时候）
                totalBitNum += 1+prefix.length() + theTagLength;
            }

            int theColliedNum = 0;                 // 用于存储碰撞位个数
            if(theTagLength >= L){
                logger.info("------此时未到最后一块L----------");
                for(int i = 0;i<L;i++){            // 逐位比较，获取碰撞位个数
                    char theBit = '2';
                    for(MBI_Tag t : input.MBITagList){
                        if(!t.slot.tagID.equals("")){
                            char theBitNow = t.slot.tagID.charAt(i);
                            if(theBit == '2'){
                                theBit = theBitNow;
                                s_store = t.slot.tagID.substring(0,L);
                            }else{
                                if(theBit != theBitNow){
                                    if(theColliedNum == 0){
                                        s2_store = prefix + t.slot.tagID.substring(0,i);
                                    }
                                    theColliedNum++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }else{
                logger.info("----------此时已经到了最后一块L------------");
                for(int i = 0;i<theTagLength;i++){            // 逐位比较，获取碰撞位个数(此时，剩余长度要小于L)
                    char theBit = '2';
                    for(MBI_Tag t : input.MBITagList){
                        if(!t.slot.tagID.equals("")){
                            char theBitNow = t.slot.tagID.charAt(i);
                            if(theBit == '2'){
                                theBit = theBitNow;
                                s_store = t.slot.tagID;
                            }else{
                                if(theBit != theBitNow){
                                    if(theColliedNum == 0){
                                        s2_store = prefix + t.slot.tagID.substring(0,i);
                                    }
                                    theColliedNum++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(theColliedNum == 0){               // 未发生碰撞，即这一个L区块内的值一样或只有这一块,利用之前存储的s_store
                logger.info("-------未发生碰撞---------");
                prefix = prefix + s_store;          // 修改前缀值，如果是最后一块，则将其加入到识别标签中；如果不是，则把它作为虚拟前缀值重新循环
                if(prefix.length() == theWholeTagLength){      // 如果这就是最后一块，说明当前L内值为这个的只有这一个标签，进行识别操作
                    logger.info("----这就是最后一块L，此时完成识别--------");
                    tagIDList.add(prefix);
                    logger.info("-----识别的TagID为" + prefix + "--------");
                    // prefix清空以备后续写入
                    prefix = "";
                }else{
                    /*
                    这儿特殊一些进行注释
                    如果这一块L不是最后一块， 则说明当前L之内只能说是值一样，不能确定识别标签
                    此时过程在读写器内完成，利用之前传输内容进行后续操作
                    在本代码中为方便起见，以更新前缀模拟一次传输比较简便地实现该过程，实际上在这儿并没有再次发生一次数据传输
                     */
                    logger.info("------这不是最后一块L，此时说明该L内全相同-----");
                    theL = 1;      // 块统计标1，此后进行的一次数据传输（即对下一块L的）为模拟，实际未发生
                    continue;
                }
            }else if(theColliedNum == 1){           // 发生了碰撞，但只有一个碰撞位，并可以确认该碰撞位位置,利用s2_store
                logger.info("-------发生碰撞但只有一个碰撞位，可识别---------");
                String prefix0 = s2_store + "0" + s_store.substring(s2_store.length() + 1 - prefix.length());
                String prefix1 = s2_store + "1" + s_store.substring(s2_store.length() + 1 - prefix.length());
                if(prefix0.length() == theWholeTagLength){         // 如果这就是最后一块L范围内的，则直接识别，并重置prefix
                    logger.info("------这就是最后一块L，则识别俩个TagID，为" + prefix0 + " 和 " + prefix1);
                    tagIDList.add(prefix0);
                    tagIDList.add(prefix1);
                    prefix = "";
                }else{                               //   如果这不是最后一块L范围内，则将增长了的prefix放入堆栈
                    logger.info("------这不是最后一块L，堆栈加入俩个prefix，分别为" + prefix0 + " 和 " + prefix1);
                    stack.push(prefix0);
                    stack.push(prefix1);
                    prefix = "";                 //   重置prefix
                }
            } else if(theColliedNum > 1){            // 发生了超过一个的碰撞位，则进入M-Slot模式
                // 这是 M - SLOT 模式，继承B-Slot的prefix，重新开始通信
                //  按照对比要求的一次问答记为一次slot，此处将M-Slot与之前的B-Slot分开，分别计算一次Slot
                logger.info("=============该slot进入M-slot模式===============");
                logger.info("------------继承得到的prefix为" + prefix + "---------");
                logger.info("---------标签重新通信，响应的标签的有关信息如下----------");
                for(MBI_Tag t:input.MBITagList){          // 标签读取器通信
                    t.slot.tagID ="";
                    t.slot.feedback = "1" + prefix;
                    t.transmitID();
                    logger.info("--------feedback为" + t.slot.feedback + " ,tagID为" + t.slot.tagID);
                    if(!t.slot.tagID.equals("")){
                        theTagLength = t.slot.tagID.length();
                    }
                }
                // 在该M-Slot中，标签重新通信，进行的问答传输比特数目
                totalBitNum += 1 + prefix.length() + theTagLength;
                List<Integer> L_Store = new ArrayList<>();     // 存储读取器从标签中获得的信息时发生碰撞时每一个碰撞位的位置
                String str = "";                              // 设置字符串用于保存一个字符串内容
                for(int i = 0;i<theTagLength;i++){             // 找出各个响应在哪儿发生了碰撞，并存入L_Store
                    char theBit = '2';
                    for(MBI_Tag t:input.MBITagList){
                        if(!t.slot.tagID.equals("")){
                            char theBitNow = t.slot.tagID.charAt(i);
                            if(theBit == '2'){
                                theBit = theBitNow;
                                str = t.slot.tagID;
                            }else{
                                if(theBit != theBitNow){
                                    L_Store.add(i);
                                    break;
                                }
                            }
                        }
                    }
                }
                logger.info("----根据通信的Tag发送过来的信息，找到的碰撞位为-------");
                for(Integer i : L_Store){                      //  对于每一个碰撞位，读取器相应的根据对应的公共字符串生成其对应的ID片段，完成识别
                    int number = i/L;                            //  该值指示了该碰撞位对应哪一个公共字符串（CS都是引用的Main2里的实例，其实体一致，可直接通过序号确认）
                    int number2 = i%L;                         //   该值指示在L段内该碰撞位的位置
                    logger.info("-----第" + number + "个公共字符串内的第" + number2 + "位--------" );
                    StringBuilder st = new StringBuilder();
                    for(int j = 0;j<number2;j++){
                        st.append(0);
                    }
                    st.append(1);
                    for(int j = number2 + 1;j<L;j++){
                        st.append(0);
                    }                                            //  到此，生成了一个在该L片段内除了碰撞位为1其他都是0的字符串
                    String s = MBI_Entrance.XOR(CS_Memory.get(number),st.toString());    // 该字符串与对应的公共字符串进行XOR运算，即可得到原本的ID片段（可逆性）
                    logger.info("还原得到的该L内的字符为 " + s );
                    if(prefix.length() + s.length() == theWholeTagLength){       // 如果该片段为最后一个片段，则直接识别完成；如果不是，加入到前缀堆栈中去
                        logger.info("该L是最后一块L，识别该TagID，为" + prefix + s);
                        tagIDList.add(prefix + s);
                    }else{
                        logger.info("该L不是最后一块L，加入堆栈，加入的前缀为" + prefix + s);
                        stack.add(prefix + s);
                    }
                }
                prefix = "";           // 重置prefix，下次循环正常进入B-slot
                theM_SlotNum++;
            }
            theL = 0;  // 归零块统计，用于计算传输比特数（防止受到虚构传输的影响）
            if(!stack.isEmpty())         //  最后一步，判断堆栈是否为空，如果为空，则不操作；非空则弹出一个前缀字段给prefix
                prefix = stack.pop();
            slotNum++;
        } while (prefix != "" || !stack.isEmpty());           //  循环的终止条件即为当循环到快要结束时，发现stack为空，此时prefix已被重置为空，此时循环结束
        slotNum = slotNum + theM_SlotNum;
        logger.info("------识别结束，此时的Slot数目为"+slotNum +",M-Slot数目为" + theM_SlotNum + "-----");
        logger.info("------识别了的标签ID数目为：" + tagIDList.size() + "----------");
        for(String s:tagIDList){
            logger.info("-----ID为" + s + "---------");
        }
        totalTime = 5*totalBitNum;
    return new MBI_Output(totalTime,totalBitNum,slotNum);
    }
}
