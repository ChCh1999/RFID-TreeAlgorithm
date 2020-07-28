package MRBerror;

import MRBerror.MRB_Main;
import MRBerror.MRB_Reader;
import base.Tag;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MRB_Main Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>5月 4, 2020</pre>
 */
public class MRB_MainTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: 策略2
     */
    @Test
    public void testS2() throws Exception {
        MRB_Reader.logger.info("最小唯一碰撞集沉默");
        MRB_Main.mutilSessionTest(2, "test", "log/");
    }

    /**
     * Method: 策略3 精确沉默
     */
    @Test
    public void testS3() throws Exception {
        MRB_Reader.logger.info("精确沉默");
        MRB_Main.mutilSessionTest(3, "test", "log" + File.separator);
    }

    @Test
    public void testInfinty() {
//    System.out.println(1.0/0);
    }

    @Test
    public void testS5() {
        MRB_Main.mutilSessionTest(5, "t5", "log/s5");
    }

    @Test
    public void testS4() {
        MRB_Main.tagCount=30;
        MRB_Main.mutilSessionTest(4, "t4", "log/s5");
    }


    /**
     * 从文件中读取字符串List，文件每行一个字符串
     * @param filePath  文件路径
     * @return  读取到的字符串List
     */
    public static List<String> readTxtFileIntoStringArrList(String filePath)
    {
        List<String> list = new ArrayList<String>();
        try
        {
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null)
                {
                    list.add(lineTxt);
                }
                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("找不到指定的文件");
            }
        }
        catch (Exception e)
        {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

        return list;
    }


    @Test
    public void testFixedTagMultiTime(){
        int c = 1000;

        MRB_Reader r = new MRB_Reader();
        double avgRes = 0;
        List<String> tagStrings = readTxtFileIntoStringArrList("out1.txt");
        List<MRB_Tag> tags = new ArrayList<>();
        for ( String s: tagStrings) {
            MRB_Tag tag = new MRB_Tag(s);
            tag.ASC = tags.size();
            tags.add(tag);
        }

        for (int round = 0; round < c; round++){
            List<DataRecord> resTemp = r.MultiSession(tags, 5, 0.005);
            double p = resTemp.get(resTemp.size() - 1).p;
            avgRes += p;
            if (round % 100 == 0 ){
                System.out.println(round);
            }

        }
        fileUtil.transferData2Json(".\\log\\0727_4\\testFixedTagsMultiTime.txt");
        try {
            r.fileWriterFeng.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void testFixedTags(){
        MRB_Reader r = new MRB_Reader();
        double avgRes = 0;
/*//        统一的标签集
        MRB_Input input = new MRB_Input(MRB_TagGenerator.generateTag(tagIDlength, tagCount));*/

//        for (int i = 0; i < 1; i++) {
//            List<DataRecord> resTemp = r.MultiSession(Objects.requireNonNull(MRB_TagGenerator.generateTag(tagIDLength, tagCount)), silenceStrategy, thresholdPM);
//            double p = resTemp.get(resTemp.size() - 1).p;
//            avgRes += p;
//        }

        List<String> tagStrings = readTxtFileIntoStringArrList("out1.txt");
        List<MRB_Tag> tags = new ArrayList<>();
        for ( String s: tagStrings) {
            MRB_Tag tag = new MRB_Tag(s);
            tag.ASC = tags.size();
            tags.add(tag);
        }

        List<DataRecord> resTemp = r.MultiSession(tags, 5, 0.005);
        double p = resTemp.get(resTemp.size() - 1).p;
        avgRes += p;

        try {
            r.fileWriterFeng.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        fileUtil.transferData2Json("testFixedTags.txt");

//        System.out.println("avg: " + avgRes / roundCount);
//        return avgRes / 10;
    }
} 
