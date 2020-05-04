package MRBerror;

import MRBerror.MRB_Reader;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After; 

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
* Method: main(String[] args) 
* 
*/ 
@Test
public void testS2() throws Exception {
    MRB_Reader.logger.info("最小唯一碰撞集沉默");
    MRB_Main.mutilSessionTest(2, "test");
} 


} 
