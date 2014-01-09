package org.camunda.bpm.cycle.roundtrip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.cycle.util.IoUtil;
import org.junit.Test;

public class BpmnProcessModelUtilTest extends AbstractRoundtripTest {

  @Test
  public void testReplaceDeveloperFriendlyIds() {
    String sourceModel = IoUtil.readFileAsString("org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn");

    String resultModel = roundtripUtil.replaceDeveloperFriendlyIds(sourceModel);
    Assert.assertTrue(resultModel.contains("Process_Engine"));
  }

  @Test
  public void testReplaceDeveloperFriendlyIdsWithReplacePoolId() {
    String sourceModel = IoUtil.readFileAsString("org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn");

    String resultModel = roundtripUtil.replaceDeveloperFriendlyIds(sourceModel,"My Custom Pool Name");
    Assert.assertTrue(resultModel.contains("My Custom Pool Name"));
  }

  @Test
  public void testExtractPool() throws IOException {
    InputStream sourceModel = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn"));

    String resultModel = IOUtils.toString(roundtripUtil.extractExecutablePool(sourceModel), "UTF-8");
    Assert.assertFalse(resultModel.contains("Mensch"));
  }

  @Test
  public void testShouldRemoveCollapsedPool() throws IOException {
    InputStream sourceModel = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/repository/test-lhs-with-collapsed-pool.bpmn"));

    String resultModel = IOUtils.toString(roundtripUtil.extractExecutablePool(sourceModel), "UTF-8");
    Assert.assertFalse(resultModel.contains("A collapsed Pool"));
    Assert.assertFalse(resultModel.contains("sid-099E1FA7-EDE2-48DB-B6FF-D06E54C58C70"));
  }
  
  @Test
  public void testShouldContainCollapsedPool() throws IOException {
    String sourceModel = IOUtils.toString(new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/repository/test-rhs-with-collapsed-pool.bpmn")), "UTF-8");
    String targetModel = IOUtils.toString(new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/repository/test-lhs-with-collapsed-pool.bpmn")), "UTF-8");

    String resultModel = roundtripUtil.importChangesFromExecutableBpmnModel(sourceModel, targetModel);
    Assert.assertTrue(resultModel.contains("A collapsed Pool"));
    Assert.assertTrue(resultModel.contains("sid-099E1FA7-EDE2-48DB-B6FF-D06E54C58C70"));
    Assert.assertTrue(resultModel.contains("A new Service Task"));
  }

}
