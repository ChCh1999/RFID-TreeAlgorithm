package QT;

import QT.QT_Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QT_TagGenerator {
    /*
    根据输入的标签数和标签ID长度生成随机分布的标签
     */
    public static List<QT_Tag> generateTag(int tagNum, int tagIDLength) {
        List<QT_Tag> QTTagList = new ArrayList<>();
        while (QTTagList.size() < tagNum) {
            QT_Tag t = new QT_Tag(tagIDLength);
            // 确保没有重复标签
            boolean hasRepeat = false;
            for (QT_Tag t1 : QTTagList) {
                if (t.ID.equals(t1.ID)) hasRepeat = true;
            }
            if (!hasRepeat)
                QTTagList.add(new QT_Tag(tagIDLength));
        }
        return QTTagList;
    }

    /*
    在动态环境下生成标签，输入为新来标签的比例和离开标签的比例
     */
    public static List<QT_Tag> generateTag(List<QT_Tag> QTTagList, double arrivingRadio, double stayingRadio) {
        int currentTagNum = QTTagList.size();
        int arrivingTagNum = (int) (arrivingRadio * currentTagNum);
        int stayingTagNum = (int) (stayingRadio * arrivingRadio);
        int tagIDLength = QTTagList.get(0).ID.length();
        // 删去离开的标签
        for (int i = 0; i < currentTagNum - stayingTagNum; i++) {
            int index = new Random().nextInt(QTTagList.size());
            QTTagList.remove(index);
        }
        // 加上新来的标签
        while (QTTagList.size() < stayingTagNum + arrivingTagNum) {
            QT_Tag t = new QT_Tag(tagIDLength);
            // 确保没有重复标签
            boolean hasRepeat = false;
            for (QT_Tag t1 : QTTagList) {
                if (t.ID.equals(t1.ID)) hasRepeat = true;
            }
            if (!hasRepeat)
                QTTagList.add(new QT_Tag(tagIDLength));
        }
        return QTTagList;
    }
}
