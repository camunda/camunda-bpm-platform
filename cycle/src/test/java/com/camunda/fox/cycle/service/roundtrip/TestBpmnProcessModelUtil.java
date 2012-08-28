package com.camunda.fox.cycle.service.roundtrip;

import junit.framework.Assert;

import org.junit.Test;

import com.camunda.fox.cycle.util.IoUtil;


public class TestBpmnProcessModelUtil {
  
  private BpmnProcessModelUtil cycleRoundtripUtil = new BpmnProcessModelUtil();
  
  @Test
  public void testReplaceDeveloperFriendlyIds() {
    String sourceModel = IoUtil.readFileAsString("com/camunda/fox/cycle/service/roundtrip/collaboration.bpmn");
        
    String resultModel = cycleRoundtripUtil.replaceDeveloperFriendlyIds(sourceModel);
    Assert.assertTrue(resultModel.contains("Process_Engine"));
  }
  
  @Test
  public void testReplaceDeveloperFriendlyIdsWithReplacePoolId() {
    String sourceModel = IoUtil.readFileAsString("com/camunda/fox/cycle/service/roundtrip/collaboration.bpmn");
        
    String resultModel = cycleRoundtripUtil.replaceDeveloperFriendlyIds(sourceModel,"My Custom Pool Name");
    Assert.assertTrue(resultModel.contains("My Custom Pool Name"));
  }
  
  @Test
  public void testExtractPool() {
    String sourceModel = IoUtil.readFileAsString("com/camunda/fox/cycle/service/roundtrip/collaboration.bpmn");
        
    String resultModel = cycleRoundtripUtil.extractExecutablePool(sourceModel);
    System.out.println(resultModel);
  }

}
