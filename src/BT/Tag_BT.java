package BT;

import base.Tag;

import java.util.Random;

/**
 * Created by limingzhe in 20:22 2017/5/5.
 * Description: BT算法，标签拥有counter，每一轮过程中初始化为0，counter=0的标签才能发送ID；
 *              发送碰撞的话，在该slot发送ID的标签随机+0或+1，没有发送ID的标签+1；
 *              没有发送碰撞，所有标签counter-1；
 *              counter为负数说明该标签已经被识别，不再进行任何操作
 * Ref: Myung J, Lee W, Srivastava J, et al.
 *      Tag-splitting: adaptive collision arbitration protocols for RFID tag identification[J].
 *      IEEE transactions on parallel and distributed systems, 2007, 18(6): 763-775.
 */
public class Tag_BT extends Tag {
    /**
     * 构造函数
     * @param tagIDLength 标签长度
     */
    public Tag_BT(int tagIDLength) {
        super(tagIDLength);
        counter = 0;
    }

    /**
     * 构造函数
     * @param tagIDLength 标签长度
     * @param x_range x坐标
     * @param y_range y坐标
     */
    public Tag_BT(int tagIDLength, double x_range, double y_range) {
        super(tagIDLength, x_range, y_range);
        counter = 0;
    }

    /**
     * 计数器，用于分类标签集合
     */
    public int counter;

    /**
     * 开始一轮识别
     */
    public void startFrame() {
        counter = 0;
    }

    /**
     * 得到反馈后进行的处理
     */
    public void handleAfterSlot() {
        // tag recognized by a reader does not transmit any single
        if (counter >= 0)
            if (slot.feedback.equals("idle"))
                counter -= 1;
            else if (slot.feedback.equals("readable"))
                counter -= 1;
            else {
                if (counter == 0)
                    counter += new Random().nextInt(2);
                else
                    counter += 1;
            }
    }

    /**
     * 重载传送ID的方法
     */
    @Override
    public void transmitID() {
        if (counter == 0)
            slot.tagID = ID;
    }

    @Override
    public String toString() {
        return "tag's ID is " + ID + ", tag's counter = " + counter;
    }
}
