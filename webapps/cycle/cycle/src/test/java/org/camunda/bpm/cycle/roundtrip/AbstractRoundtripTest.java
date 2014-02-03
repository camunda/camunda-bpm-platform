package org.camunda.bpm.cycle.roundtrip;

import org.camunda.bpm.cycle.util.IoUtil;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static org.camunda.bpm.cycle.util.IoUtil.readFileAsString;

/**
 *
 * @author Nico Rehwaldt
 */
public class AbstractRoundtripTest {

  protected BpmnProcessModelUtil roundtripUtil = new BpmnProcessModelUtil();

  protected String importExecutableModel(String executableDiagramXml, String targetDiagramXml) {
    return roundtripUtil.importChangesFromExecutableBpmnModel(executableDiagramXml, targetDiagramXml);
  }

  protected String importExecutableModelFileBased(String executablePath, String targetPath) {
    String executableXml = IoUtil.readFileAsString(executablePath);
    String targetXml = IoUtil.readFileAsString(targetPath);

    return importExecutableModel(executableXml, targetXml);
  }

  protected String extractExecutableModel(String fromDiagramXml) {
    return roundtripUtil.extractExecutablePool(fromDiagramXml);
  }

  protected String extractExecutableModelFromFile(String path) {
    InputStream stream = roundtripUtil.extractExecutablePool(IoUtil.readFileAsInputStream(path));

    try {
      return new String(IoUtil.readInputStream(stream, "foo"), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  protected String normalizeXml(String body) {
    return body.replaceAll("[\\n\\r]+", "")
        .replaceAll("[ ]+", " ");
  }

  protected String getFileContents(String sourceDiagram) {
    return readFileAsString(sourceDiagram);
  }
}
