package MRBerror;

import QT.QT_Tag;

import java.rmi.MarshalException;
import java.util.*;

public class MRB_TagGenerator {
    public static List<MRB_Tag> generateTag(int len,int count){
        ArrayList<MRB_Tag> MRBTagList=new ArrayList<MRB_Tag>();
        while (MRBTagList.size() < count) {
            MRB_Tag t = new MRB_Tag(len);
            // 确保没有重复标签
            boolean hasRepeat = false;
            for (MRB_Tag t1 : MRBTagList) {
                if (t.ID.equals(t1.ID)) hasRepeat = true;
            }
            if (!hasRepeat){
                t.ASC= MRBTagList.size();
                MRBTagList.add(t);
            }
        }
        MRB_Reader.logger.info("构造标签集"+MRBTagList);
        return MRBTagList;
    }
}
