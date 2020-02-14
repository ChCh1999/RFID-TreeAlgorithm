package MBI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MBI_Entrance {
    // 此处为程序入口，在进行识别之前，该算法要求完成公共字符串的计算并保存到每个标签和读取器
    public MBI_Output entrance(MBI_Input input){
        // 在此根据L的值将CS_Memory推算出来，并进行存入(L的值为4的整数倍，推荐为4或8，可修改)
        int L = 4;
        List<String> CS_Memory = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < L-1;i++){
            sb.append('0');
        }
        sb.append('1');
        //System.out.println(sb.toString());
        // 将初始化的code放入H(即CS_Memory)中
        CS_Memory.add(sb.toString());

        // 开始循环

        while (CS_Memory.size() != Math.pow(2,L)/L-1){
            // 寻找符合条件的code E
            String E = "";
            int a = -1;    // 用来终止循环的内部参数
            while(a != 0){
                a = 0;
                StringBuilder stb = new StringBuilder();
                for(int i = 0; i<L-1;i++){
                    stb.append(new Random().nextInt(2));
                }
                stb.append(1);
                E = stb.toString();
                for(String s : CS_Memory){
                    if(GetCodeDistence(E,s) <= 2){
                        a++;
                    }
                }
            }
            // 此时已经得到了E
            //System.out.println("E is " + E);
            List<String> store = new ArrayList<>();
            for(String s:CS_Memory){
                store.add(XOR(E,s));
                //System.out.println("add the " + XOR(E,s) + " in to the cs");
            }
            CS_Memory.addAll(store);
            CS_Memory.add(E);
        }
        StringBuilder sri = new StringBuilder();
        for(int i = 0;i<L;i++){
            sri.append(0);
        }
        CS_Memory.add(sri.toString());


        // 以经完成了CS_Memory的获取,现将其写入Reader中，与之同时写入的还有L
        MBI_Reader mbi_reader = new MBI_Reader(L,CS_Memory);
        // 生成标签群体，并在main函数范围内，识别开始之前，将CS_Memory写入每一个标签内部。此处实体由Main2持有，将调用写入
        for(MBI_Tag tag:input.MBITagList){
            tag.L = L;
            tag.CS_Memory = CS_Memory;
        }
        return mbi_reader.Distinguish(input);
    }
    // 定义XOR运算方法
    public static String XOR(String s1,String s2){
        if(s1.length() != s2.length()){
            System.out.println("ERROR in " + s1 + " XOR "+ s2);
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<s1.length();i++){
            if(s2.charAt(i) == '0')
                sb.append(s1.charAt(i));
            else{
                if(s1.charAt(i) == '0')
                    sb.append(1);
                else
                    sb.append(0);
            }
        }
        return sb.toString();
    }
    // 获取code distance即俩个字符串碰撞位个数的方法
    public static int GetCodeDistence(String s1,String s2){
        if(s1.length() != s2.length()){
            System.out.println("ERROR in " + s1 + " GetCodeDistence "+ s2);
            return -1;
        }
        int num = 0;
        for(int i = 0; i<s1.length();i++){
            if(s1.charAt(i) != s2.charAt(i))
                num++;
        }
        return num;
    }
}
