package SRB;

import ABS.Tag_ABS;

import java.util.Random;

/**
 * Created by limingzhe in 19:01 2017/5/9.
 * Description: SRB标签基于ABS标签，并使用了阻塞算法，即使用tRID将新来标签的ASC和原有标签的ASC区分开
 *              新来标签的ASC随机选择TSC+1~TSCEXT的值作为ASC
 * Ref: Lai Y C, Lin C C.
 *      Two blocking algorithms on adaptive binary splitting: single and pair resolutions for RFID tag identification[J].
 *      IEEE/ACM Transactions on Networking (TON), 2009, 17(3): 962-975.
 */
public class Tag_SRB extends Tag_ABS {
    /**
     * 标签所属的阅读器ID
     */
    public int tRID;

    public Tag_SRB(int tagIDLength) {
        super(tagIDLength);
        tRID = -1;   // it hasn't been recognized by any readers
    }

    public Tag_SRB(int tagIDLength, double x_range, double y_range) {
        super(tagIDLength, x_range, y_range);
        tRID = -1;
    }

    public void startFrame(int TSC, int TSCEXT, int rRID) {
        PSC = 0;
        if (tRID != rRID || tRID == -1) {
            if (TSC == TSCEXT)
                ASC = TSC;
            else
                // ASC choose TSC+1~TSCEXT
                ASC = new Random().nextInt(TSCEXT - TSC) + TSC + 1;
            tRID = rRID;
        }
    }

    @Override
    public String toString() {
        return "tag's ID is " + ID + ", PSC = " + PSC + ", ASC = " + ASC + ", tRID = " + tRID;
    }
}
