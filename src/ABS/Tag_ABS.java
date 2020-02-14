package ABS;

import base.Tag;

import java.util.Random;

/**
 * Created by limingzhe in 19:28 2017/5/8.
 * Description: ABS算法的标签有ASC和PSC两个计数器，PSC记录了当前已经被识别标签的个数，
 *              每个slot ASC=PSC的标签发送ID，然后根据反馈修改ASC和PSC的值，
 *              一轮识别完成后，N个标签的ASC分别为0~N-1
 * Ref: Myung J, Lee W, Srivastava J, et al.
 *      Tag-splitting: adaptive collision arbitration protocols for RFID tag identification[J].
 *      IEEE transactions on parallel and distributed systems, 2007, 18(6): 763-775.
 */
public class Tag_ABS extends Tag {

    public Tag_ABS(int tagIDLength) {
        super(tagIDLength);
        ASC = -1;
        PSC = -1;
    }

    public Tag_ABS(int tagIDLength, double x_range, double y_range) {
        super(tagIDLength, x_range, y_range);
        ASC = -1;   // it hasn't been recognized by any readers
        PSC = -1;
    }

    public int ASC;
    public int PSC;

    public void startFrame(int TSC) {
        PSC = 0;
        if (ASC == -1 || ASC > TSC) {
            if (TSC == 0)
                ASC = 0;
            else
                // ASC choose 0~TSC
                ASC = new Random().nextInt(TSC + 1);
        }
    }

    public void handleAfterSlot() {
        // tag recognized by a reader does not transmit any single
        if (PSC <= ASC) {
            if (PSC == ASC)
                if (slot.feedback.equals("collision"))
                    ASC += new Random().nextInt(2);
                else
                    PSC += 1;
            else
                if (slot.feedback.equals("collision"))
                    ASC++;
                else if (slot.feedback.equals("readable"))
                    PSC++;
                else
                    ASC--;
        }
    }

    @Override
    public void transmitID() {
        if (PSC == ASC)
            slot.tagID = ID;
    }

    @Override
    public String toString() {
        return "tag's ID is " + ID + ", tag's PSC = " + PSC + ", ASC = " + ASC;
    }
}
