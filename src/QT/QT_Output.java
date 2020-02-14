package QT;

/*
每个算法的输出类，包含了评价这些算法的指标
 */
public class QT_Output {
    /*
    Slot数目
     */
    public int slotNum;

    /*
    空时隙的数目
     */
    public int idleSlotNum;

    /*
    可读时隙数目
     */
    public int readableSlotNum;

    /*
    碰撞时隙数目
     */
    public int collisionSlotNum;

    /*
    总时间
     */
    public double totalTime;

    /*
    空时隙耗费的时间
     */
    public double idleTime;

    /*
    可读时隙耗费的时间
     */
    public double readableTime;

    /*
    碰撞时隙耗费的时间
     */
    public double collisionTime;

    /*
    传送的所有比特数
     */
    public int totalBitNum;

    /*
    空时隙传送的比特数
     */
    public int idleBitNum;

    /*
    可读时隙传送的比特数
     */
    public int readableBitNum;

    /*
    碰撞时隙传送的比特数
     */
    public int collisionBitNum;

    public QT_Output(int slotNum, int idleSlotNum, int readableSlotNum, int collisionSlotNum, double totalTime, double idleTime, double readableTime, double collisionTime, int totalBitNum, int idleBitNum, int readableBitNum, int collisionBitNum) {
        this.slotNum = slotNum;
        this.idleSlotNum = idleSlotNum;
        this.readableSlotNum = readableSlotNum;
        this.collisionSlotNum = collisionSlotNum;
        this.totalTime = totalTime;
        this.idleTime = idleTime;
        this.readableTime = readableTime;
        this.collisionTime = collisionTime;
        this.totalBitNum = totalBitNum;
        this.idleBitNum = idleBitNum;
        this.readableBitNum = readableBitNum;
        this.collisionBitNum = collisionBitNum;
    }
}
