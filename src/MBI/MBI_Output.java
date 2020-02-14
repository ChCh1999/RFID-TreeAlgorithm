package MBI;

public class MBI_Output {
    /*
    总时间
     */
    public double totalTime;

    /*
    传送的所有比特数
     */
    public int totalBitNum;

    /*
    Slot数目
     */
    public int slotNum;

    public MBI_Output(double totalTime, int totalBitNum, int slotNum) {
        this.totalTime = totalTime;
        this.totalBitNum = totalBitNum;
        this.slotNum = slotNum;
    }
}
