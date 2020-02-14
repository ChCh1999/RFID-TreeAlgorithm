package QT;

import QT.QT_Input;
import QT.QT_Reader;
import QT.QT_TagGenerator;

public class Main {
    /*
    主函数，测试QT算法
     */
    public static void main(String[] args) {
        QT_Reader r = new QT_Reader();
        r.oneFrame(new QT_Input(QT_TagGenerator.generateTag(5, 10)));
    }
}
