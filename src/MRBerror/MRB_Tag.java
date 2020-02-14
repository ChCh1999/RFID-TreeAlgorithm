package MRBerror;

import java.util.Random;

public class MRB_Tag {
    // 标签ID
    public String ID;
    // 对应算法的ASC
    public int ASC;
    public Slot slot;
    // 标志该标签是否被沉默。默认值为true，为false表示在本周期其沉默
    public boolean use;
    /**
     * 构造函数，可以根据传入的标签长度随机生成标签ID
     * @param tagIDLength 标签ID长度
     */
    public MRB_Tag(int tagIDLength) {
        // randomly init ID
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();
        ASC=-1;
        use = true;
        slot = new Slot();
    }

    public MRB_Tag(String ID) {
        this.ID = ID;
        ASC= -1;    // -1表示该标签未被识别
        slot = new Slot();
        use = true;
    }

    public MRB_Tag(String ID, int ASC) {
        this.ID = ID;
        this.ASC = ASC;
        slot = new Slot();
        use = true;
    }

    public MRB_Tag(int tagIDLength, int ASC) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();
        this.ASC = ASC;
        slot = new Slot();
        use = true;
    }

    @Override
    public String toString() {
        return this.ID + ", " + this.ASC;
    }

    public int PSC=0;
    public int groupSize=0;
    // 标签接收读取器广播并向信道输入信息
    public void receiveReaderRequest(){
        int num = slot.receive.indexOf("|");
        PSC = Integer.parseInt(slot.receive.substring(0,num));
        groupSize = Integer.parseInt(slot.receive.substring(num+1,slot.receive.length()));
        if(ASC>=PSC&&ASC<PSC+groupSize)
            slot.msg = ID;
    }

    /**
     * 模拟标签与读取器在标签端的通信
     */
    public class Slot {
        public String msg;
        public String receive;

        public Slot() {
            msg = "";
            receive = "";
        }

        public String getSlotInfo() {
            return "msg is " + msg + ", receive is " + receive;
        }
    }
}
