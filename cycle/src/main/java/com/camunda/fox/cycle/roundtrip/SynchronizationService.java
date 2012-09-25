package com.camunda.fox.cycle.roundtrip;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.util.IoUtil;



/**
 * Service encapsulating the synchronization magic done in cycle.
 * Works on input / output streams, not connector nodes.
 * 
 * @author nico.rehwaldt
 */
@Component
public class SynchronizationService {

  private BpmnProcessModelUtil processModelUtil = new BpmnProcessModelUtil();
  
  /**
   * Given the streams of two bpmn 2.0 diagrams, perform the 
   * synchronization from left to right. Return an input stream to the resulting
   * right hand side file.
   * 
   * @param lhsInputStream
   * @param rhsInputStream
   * 
   * @throws IOException
   * @return 
   */
  public InputStream syncLeftToRight(InputStream lhsInputStream, InputStream rhsInputStream) throws IOException {
    return processModelUtil.extractExecutablePool(lhsInputStream);
  }
  
  /**
   * Given the streams of two bpmn 2.0 diagrams, perform the 
   * synchronization from right to left. Return an input stream to the resulting
   * left hand side file.
   * 
   * @param lhsInputStream
   * @param rhsInputStream
   * 
   * @throws IOException
   * @return 
   */
  public InputStream syncRightToLeft(InputStream lhsInputStream, InputStream rhsInputStream) throws IOException {
    String result = processModelUtil.importChangesFromExecutableBpmnModel(IoUtil.toString(rhsInputStream, "UTF-8"), IoUtil.toString(lhsInputStream, "UTF-8"));
    return IoUtil.toInputStream(result, "UTF-8");
  }
}
