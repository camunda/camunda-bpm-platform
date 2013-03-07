package org.camunda.bpm.cycle.roundtrip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.roundtrip.SynchronizationService;
import org.camunda.bpm.cycle.util.IoUtil;
import org.junit.Test;


public class SynchronizationServiceTest {
  
  private SynchronizationService synchronizationService = new SynchronizationService();
  
  @Test
  public void shouldThrowExceptionWhenSyncFromLeftToRight() throws IOException {
    InputStream lhsInputStream = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/ibo-invoice.bpmn"));
    InputStream rhsInputStream = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/ibo-invoice-designer.bpmn"));
    
    try {
      synchronizationService.syncLeftToRight(lhsInputStream, rhsInputStream);
      Assert.fail("A CycleException was excpected.");
    } catch (CycleException e) {
    }
  }

  @Test
  public void shouldThrowExceptionWhenSyncFromRightToLeft() throws IOException {
    InputStream lhsInputStream = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/ibo-invoice.bpmn"));
    InputStream rhsInputStream = new FileInputStream(IoUtil.getFile("org/camunda/bpm/cycle/roundtrip/ibo-invoice-designer.bpmn"));
    
    try {
      synchronizationService.syncRightToLeft(lhsInputStream, rhsInputStream);
      Assert.fail("A CycleException was excpected.");
    } catch (CycleException e) {
    }
  }
  
}
