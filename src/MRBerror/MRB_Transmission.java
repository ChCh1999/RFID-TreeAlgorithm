package MRBerror;

import java.util.List;
import java.util.Random;

import static MRBerror.MRB_Main.rError;
import static MRBerror.MRB_Main.tError;

public class MRB_Transmission {
    // 广播方法，读取器向标签进行广播
    public static void Broadcast(MRB_Reader reader, List<MRB_Tag> MRBList) {
        for (MRB_Tag t : MRBList) {
            // 读取器到标签的动态错误，概率设置为r=20%
            int c = new Random().nextInt(100);
            // 标签可用，且未发生动态错误的标签才正确接收读取器信息
            if (t.use) {
                if (c < rError) {
                    t.slot.receive = reader.slot.broadcast;
                    t.AlertDynamicError();
                    t.slot.receive = "";
                } else {
                    t.slot.receive = reader.slot.broadcast;
                    t.receiveReaderRequest();
                    t.slot.receive = "";
                }
            }


        }
    }

    // 接受反馈方法，标签向读取器发送反馈
    public static void receiveReplies(MRB_Reader reader, List<MRB_Tag> MRBList) {
        for (MRB_Tag t : MRBList) {
            if (t.slot.msg != "") {
                // 标签到读取器的动态错误，概率设置为t=20%
                int d = new Random().nextInt(100);
                if (d < tError) {
                    MRB_Reader.logger.error("****标签（" + t + "）发送反馈时发生错误****");
                    t.slot.msg = "";
                    continue;
                } else {
                    reader.slot.add(t.slot.msg);
                    t.slot.msg = "";
                }
            }
        }
        reader.slot.addReceive();
    }
}
