package QT;

import org.apache.log4j.Logger;
import utils.Constant;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
对外接口是oneFrame函数，接受一个输入，返回一个输出
 */
public class QT_Reader {

    private static Logger logger = Logger.getLogger(QT_Reader.class);

    public QT_Reader() {
        tagIDList = new ArrayList<>();
    }

    /*
     * 识别的标签集合
     */
    public List<String> tagIDList;

    /*
     * 前缀队列
     */
    public Queue<String> prefixQueue = new LinkedList<>();


    public QT_Output oneFrame(QT_Input QTInput) {
        tagIDList.clear();  // 首先清空上一帧识别的所有标签

        List<QT_Tag> QTTagList = QTInput.QTTagList;
        int tagIDLength = QTTagList.get(0).ID.length();

        logger.info("----------帧开始----------");
        logger.info("待识别的所有标签ID：");
        for (QT_Tag t : QTTagList) {
            logger.info(t);
        }

        prefixQueue.offer("0");
        prefixQueue.offer("1");

        // slot信息
        int totalSlotNum = 0;
        int idleSlotNum = 0;
        int readableSlotNum = 0;
        int collisionSlotNum = 0;
        // 比特信息
        int totalBit = 0;
        int idleBit = 0;
        int readableBit = 0;
        int collisionBit = 0;

        // 识别过程
        while (!prefixQueue.isEmpty()) {
            // 读写器发送前缀
            String prefix = prefixQueue.poll();


            int tagIDNum = 0;   // 在这一个slot传送数据的标签数量
            String tempID = null;

            logger.info("-----该时隙读写器发送的前缀为：" + prefix);

            for (QT_Tag t : QTTagList) {
                if (t.ID.substring(0, prefix.length()).equals(prefix)) {
                    tempID = t.ID;
                    tagIDNum++;
                }
            }

            if (tagIDNum > 1) {
                logger.info("该时隙碰撞");
                prefixQueue.offer(prefix + "0");
                prefixQueue.offer(prefix + "1");
                collisionSlotNum++;
                collisionBit += (prefix.length() + tagIDLength);
            } else if (tagIDNum == 1) {
                tagIDList.add(tempID);
                logger.info(tempID + "被识别");
                readableSlotNum++;
                readableBit += (prefix.length() + tagIDLength);
            } else {
                logger.info("该时隙为空时隙");
                idleSlotNum++;
                idleBit += (prefix.length() + tagIDLength);
            }
            totalSlotNum++;
            totalBit += (prefix.length() + tagIDLength);
        }

        // 时间信息
        double idleTime = idleBit * Constant.OneBitTime;
        double readableTime = readableBit * Constant.OneBitTime;
        double collisionTime = collisionBit * Constant.OneBitTime;
        double totalTime = totalBit * Constant.OneBitTime;

        logger.info("该帧结束，被识别的标签如下：");
        for (String s : tagIDList) {
            logger.info(s);
        }
        logger.info("----------耗费的时隙数为" + totalSlotNum + "----------");
        logger.info("----------其中，空时隙为" + idleSlotNum + "----------");
        logger.info("----------其中，只读时隙为" + readableSlotNum + "----------");
        logger.info("----------其中，碰撞时隙为" + collisionSlotNum + "----------");
        logger.info("----------总的传送的比特数为" + totalBit + "----------");
        logger.info("----------其中，空时隙传送的比特数为" + idleBit + "----------");
        logger.info("----------其中，可读时隙传送的比特数为" + readableBit + "----------");
        logger.info("----------其中，碰撞时隙传送的比特数为" + collisionBit + "----------");
        logger.info("----------总的耗费的时间为" + totalTime + "----------");
        logger.info("----------其中，空时隙耗费的时间为" + idleTime + "----------");
        logger.info("----------其中，可读时隙耗费的时间为" + readableTime + "----------");
        logger.info("----------其中，碰撞时隙耗费的时间为" + collisionTime + "----------");
        prefixQueue.clear();
        return new QT_Output(totalSlotNum, idleSlotNum, readableSlotNum, collisionSlotNum,
                totalTime, idleTime, readableTime, collisionTime,
                totalBit, idleBit, readableBit, collisionBit);
    }
}
