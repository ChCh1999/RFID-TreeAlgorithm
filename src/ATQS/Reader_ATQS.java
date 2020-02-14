package ATQS;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Reader_ATQS {
    private String ID_Reader;
    private int num_Frame;
    // 前缀队列
    public Queue<String> Q;

    // 构造函数
    public Reader_ATQS(){
        StringBuilder sb = new StringBuilder();
        // 构造标签ID，长度为20
        for (int i = 0; i < 20; i++)
            sb.append(new Random().nextInt(2));
        ID_Reader = sb.toString();
        num_Frame = 0;
        Q = new LinkedList<>();
    }

    public void OneFrame(List<Tag_ATQS> tagList){
        // 第一次广播读取器ID以及帧ID
        for(Tag_ATQS t:tagList){
            t.receiveFirst(ID_Reader,String.valueOf(num_Frame));
        }

        if(!Q.isEmpty()){
            // 对驻留标签的识别
            String q1 = Q.poll();
            String qx;
            int flag_qx = 0;


            for(String s:Q){
                if(s.startsWith(q1.substring(0,q1.length()-1))){
                    qx=q1.substring(0,q1.length()-1);
                    flag_qx++;
                }
            }
        }
    }

    // 将标签ID从二进制转换成三进制表示
    public String BinaryToTernary(String s_binary){
        int s_length = s_binary.length();
        if(s_length%3==0){
            // 标签ID长度正好是三的倍数
            return getTernary(s_length,s_binary);
        } else{
            // 标签多了一或两位
            int i = s_length%3;
            String s = getTernary(s_length-i,s_binary);
            // 生成反馈的三进制标签
            s = s + s_binary.substring(s_length-i,s_length);
            return s;
        }
    }

    // 根据三的倍数的字串，生成三进制ID
    public String getTernary(int length,String s_binary){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<length;i=i+3){
            int b1,b2,b3;
            b1 = (int)s_binary.charAt(i);
            b2 = (int)s_binary.charAt(i+1);
            b3 = (int)s_binary.charAt(i+2);
            int t1=0,t2=0;
            for(int j=0;j<3;j++){
                for(int w=0;w<3;w++){
                    if(j*3+w==b1*4+b2*2+b3){
                        t1=j;
                        t2=w;
                    }
                }
            }
            sb.append(t1);
            sb.append(t2);
        }
        return sb.toString();
    }
}
