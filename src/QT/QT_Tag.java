package QT;

import java.util.Random;

public class QT_Tag {
    /**
     * 每个标签都有一个唯一的ID
     */
    public String ID;


    /**
     * 构造函数，可以根据传入的标签长度随机生成标签ID
     * @param tagIDLength 标签ID长度
     */
    public QT_Tag(int tagIDLength) {
        // randomly init ID
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagIDLength; i++)
            sb.append(new Random().nextInt(2));
        ID = sb.toString();

    }

    public QT_Tag(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return this.ID;
    }
}
