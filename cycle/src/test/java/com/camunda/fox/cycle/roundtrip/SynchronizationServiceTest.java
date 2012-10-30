package com.camunda.fox.cycle.roundtrip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.util.IoUtil;

public class SynchronizationServiceTest {
  
  private SynchronizationService synchronizationService = new SynchronizationService();
  
  @Test
  public void shouldThrowExceptionWhenSyncFromLeftToRight() throws IOException {
    InputStream lhsInputStream = new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/ibo-invoice.bpmn"));
    InputStream rhsInputStream = new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/ibo-invoice-designer.bpmn"));
    
    try {
      synchronizationService.syncLeftToRight(lhsInputStream, rhsInputStream);
      Assert.fail("A CycleException was excpected.");
    } catch (CycleException e) {
    }
  }

  @Test
  public void shouldThrowExceptionWhenSyncFromRightToLeft() throws IOException {
    InputStream lhsInputStream = new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/ibo-invoice.bpmn"));
    InputStream rhsInputStream = new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/ibo-invoice-designer.bpmn"));
    
    try {
      synchronizationService.syncRightToLeft(lhsInputStream, rhsInputStream);
      Assert.fail("A CycleException was excpected.");
    } catch (CycleException e) {
    }
  }
  
}
