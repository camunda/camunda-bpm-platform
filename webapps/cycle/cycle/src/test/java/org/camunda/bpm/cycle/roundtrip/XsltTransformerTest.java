package org.camunda.bpm.cycle.roundtrip;

import org.junit.Test;

public class XsltTransformerTest {

  private XsltTransformer xsltTransformer = XsltTransformer.instance();

  @Test
  public void shouldCreateDeveloperFriendlyTransformer() throws Exception {
    xsltTransformer.createDeveloperFriendlyTransformer();
  }

  @Test
  public void shouldCreateEnginePoolTransformer() throws Exception {
    xsltTransformer.createEnginePoolTransformer();
  }
}
