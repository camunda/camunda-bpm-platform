package org.camunda.bpm.cycle.roundtrip;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Nico Rehwaldt
 */
public class RoundtripTest extends AbstractRoundtripTest {

  @Test
  public void shouldRetainExtensionElementAttributes() throws Exception {
    String resultDiagram = roundtrip("org/camunda/bpm/cycle/roundtrip/repository/signavio-extension-elements.bpmn");

    String normalizedResultDiagram = normalizeXml(resultDiagram);

    Assert.assertTrue(normalizedResultDiagram.contains("<signavio:signavioLabel align=\"center\" bottom=\"false\" left=\"false\" ref=\"text_name\" right=\"false\" top=\"true\" valign=\"bottom\" x=\"20.0\" y=\"-8.0\"/>"));
  }

  private String roundtrip(String diagramPath) {

    String sourceModel = getFileContents(diagramPath);
    String executableModel = extractExecutableModel(sourceModel);

    return importExecutableModel(executableModel, sourceModel);
  }
}
