package com.camunda.fox.cycle.roundtrip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.camunda.fox.cycle.roundtrip.BpmnProcessModelUtil;
import com.camunda.fox.cycle.util.IoUtil;


public class BpmnProcessModelUtilTest {
  
  private BpmnProcessModelUtil cycleRoundtripUtil = new BpmnProcessModelUtil();
  
  @Test
  public void testReplaceDeveloperFriendlyIds() {
    String sourceModel = IoUtil.readFileAsString("com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn");
        
    String resultModel = cycleRoundtripUtil.replaceDeveloperFriendlyIds(sourceModel);
    Assert.assertTrue(resultModel.contains("Process_Engine"));
  }
  
  @Test
  public void testReplaceDeveloperFriendlyIdsWithReplacePoolId() {
    String sourceModel = IoUtil.readFileAsString("com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn");
        
    String resultModel = cycleRoundtripUtil.replaceDeveloperFriendlyIds(sourceModel,"My Custom Pool Name");
    Assert.assertTrue(resultModel.contains("My Custom Pool Name"));
  }
  
  @Test
  public void testExtractPool() throws IOException {
    InputStream sourceModel = new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn"));
        
    String resultModel = IOUtils.toString(cycleRoundtripUtil.extractExecutablePool(sourceModel), "UTF-8");
    Assert.assertFalse(resultModel.contains("Mensch"));
  }

}
