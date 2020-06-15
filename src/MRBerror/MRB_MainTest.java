package MRBerror;

import MRBerror.MRB_Main;
import MRBerror.MRB_Reader;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.io.File;

/** 
* MRB_Main Tester. 
* 
* @author <Authors name> 
* @since <pre>5月 4, 2020</pre> 
* @version 1.0 
*/ 
public class MRB_MainTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: 策略2
* 
*/ 
@Test
public void testS2() throws Exception {
    MRB_Reader.logger.info("最小唯一碰撞集沉默");
    MRB_Main.mutilSessionTest(2, "test","log/");
} 
/**
*
* Method: 策略3 精确沉默
*
*/
@Test
public void testS3() throws Exception {
    MRB_Reader.logger.info("精确沉默");
    MRB_Main.mutilSessionTest(3, "test","log"+ File.separator);
}

@Test
public  void testInfinty() {
//    System.out.println(1.0/0);
}
} 
