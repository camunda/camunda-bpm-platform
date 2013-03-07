package org.camunda.bpm.cycle.roundtrip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.cycle.roundtrip.BpmnProcessModelUtil;
import org.camunda.bpm.cycle.util.IoUtil;
import org.junit.Test;



public class BpmnProcessModelUtilTest {
  
  private BpmnProcessModelUtil cycleRoundtripUtil = new BpmnProcessModelUtil();
  
  @Test
  public void testReplaceDeveloperFriendlyIds() {
    String sourceModel = IoUtil.readFileAsString("org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn");
        
    String resultModel = cycleRoundtripUtil.replaceDeveloperFriendlyIds(sourceModel);
    Assert.assertTrue(resultModel.contains("Process_Engine"));
  }
  
  @Test
  public void testReplaceDeveloperFriendlyIdsWithReplacePoolId() {
    String sourceModel = IoUtil.readFileAsString("org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn");
        
    String resultModel = cycleRoundtripUtil.replaceDeveloperFriendlyIds(sourceModel,"My Custom Pool Name");
    Assert.assertTrue(resultModel.contains("My Custom Pool Name"));
  }
  
  @Test
  public void testExtractPool() throws IOException {
    InputStream sourceModel = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn"));
        
    String resultModel = IOUtils.toString(cycleRoundtripUtil.extractExecutablePool(sourceModel), "UTF-8");
    Assert.assertFalse(resultModel.contains("Mensch"));
  }

}
