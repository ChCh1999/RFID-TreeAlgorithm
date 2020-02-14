package PRB;

import SRB.Tag_SRB;

/**
 * Created by limingzhe in 21:08 2017/5/9.
 * Description: PRB标签基于SRB标签，区别在于识别原有标签的时候使用“pair resolution”，即每个slot都有两个标签发送ID，
 *              若发生碰撞，则两个标签必然都在；若可读，则另一个标签离开；若为空，则两个标签都丢失
 * Ref: Lai Y C, Lin C C.
 *      Two blocking algorithms on adaptive binary splitting: single and pair resolutions for RFID tag identification[J].
 *      IEEE/ACM Transactions on Networking (TON), 2009, 17(3): 962-975.
 */
public class Tag_PRB extends Tag_SRB {
    /**
     * 时钟控制器
     */
    public int CSC;

    /**
     * 阅读器传送过来的TSC
     */
    public int TSC;

    public Tag_PRB(int tagIDLength) {
        super(tagIDLength);
    }

    public Tag_PRB(int tagIDLength, double x_range, double y_range) {
        super(tagIDLength, x_range, y_range);
    }

    @Override
    public void startFrame(int TSC, int TSCEXT, int rRID) {
        CSC = 0;
        this.TSC = TSC;
        super.startFrame(TSC, TSCEXT, rRID);
    }

    /**
     * 识别原有标签的阶段
     */
    public void handleAfterSlotPhase1() {
        if (PSC <= ASC && CSC <= Math.floor((double) (TSC - 1) / 2)) {
            if (PSC == ASC || PSC == ASC - 1)
                if (slot.feedback.equals("collision"))
                    PSC += 2;
                else {
                    if (PSC == ASC - 1)
                        ASC--;
                    PSC++;
                }
            else if (PSC < ASC)
                if (slot.feedback.equals("collision"))
                    PSC += 2;
                else if (slot.feedback.equals("readable")) {
                    PSC++;
                    ASC--;
                } else if (slot.feedback.equals("idle"))
                    ASC -= 2;
        }

        CSC++;
    }

    /**
     * 识别原有标签阶段
     */
    public void transmitIDPhase1() {
        if (PSC == ASC || PSC == ASC - 1)
            slot.tagID = ID;
    }

    @Override
    public String toString() {
        return "tag's ID is " + ID + ", PSC = " + PSC + ", ASC = " + ASC + ", tRID = " + tRID + ", CSC = " + CSC;
    }
}
