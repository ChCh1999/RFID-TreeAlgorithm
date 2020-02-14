package OBTT;

import java.text.DecimalFormat;

public class OBTT_Main {
    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("######0.00");
        for (int tagNum = 50; tagNum <= 500; tagNum += 50) {
            double time = 0.0;
            for (int cycle = 0; cycle < 100; cycle++) {

                OBTT_Output output = new OBTT_Reader().oneFrame(new OBTT_Input(tagNum, 20));
                time += output.time;
            }
            System.out.print(df.format(time / 100 / 1000) + " ");
        }
    }
}
