package MBI;

import org.apache.log4j.PropertyConfigurator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MBI_Main {
    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("######0.00");
        for (int tagNum = 50; tagNum <= 500; tagNum += 50) {
            double time = 0.0;
            List<MBI_Tag> list = new ArrayList<>();
            while (list.size() < tagNum) {
                MBI_Tag tag = new MBI_Tag(96);
                boolean hasRepeat = false;
                for (MBI_Tag t : list) {
                    if (t.ID.equals(tag.ID)) {
                        hasRepeat = true;
                        break;
                    }
                }
                if (!hasRepeat) {
                    list.add(tag);
                }
            }
            MBI_Input input = new MBI_Input(list);
            MBI_Output output = new MBI_Entrance().entrance(input);
            time += output.totalTime;
            System.out.print(df.format(time / 100 / 1000) + " ");
        }
    }
}
