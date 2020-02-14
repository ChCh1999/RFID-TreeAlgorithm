package ATQS;

import java.util.Random;

public class Tag_ATQS {
    private String ID;
    public String ID_Reader;
    public String ID_Frame;
    private String State;

    // 构造函数，生成标签
    public Tag_ATQS(int tagIDLength){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();
    }

    // 第一次接受读取器请求，确认自身是到达标签还是驻留标签
    public void receiveFirst(String id_Reader,String id_Frame){
        if(id_Frame.equals(ID_Frame)&&id_Reader.equals(ID_Reader)){
            State="staying";
        }else
            State="arriving";
    }

    // 根据读取器发送的前缀以及当前要识别的标签状态响应读取器查询
    public String receiveSecond(String query,String State){
        if(this.State.equals(State)&&ID.startsWith(query)){
            return ID;
        }else
            return null;
    }

}
