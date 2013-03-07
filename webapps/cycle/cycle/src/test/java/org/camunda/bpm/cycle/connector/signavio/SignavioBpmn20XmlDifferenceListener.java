package org.camunda.bpm.cycle.connector.signavio;

import java.util.Arrays;
import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * XMLUnit DifferenceListener for BPMN 2.0 XML files exported from Signavio.
 *
 * In particular it ignores element ids that are always regenerated during
 * Signavio's BPMN export.
 *
 * @author Falko Menge
 */
public class SignavioBpmn20XmlDifferenceListener implements DifferenceListener {

  private List<String> elementsForWhichSignavioGeneratesNewIds = Arrays.asList(
          "definitions",
          "message",
          "error",
          "collaboration",
          "laneSet",
          "errorEventDefinition",
          "messageEventDefinition",
          "cancelEventDefinition",
          "terminateEventDefinition",
          "timerEventDefinition",
          "dataStore",
          "dataStoreReference",
          "timeCycle",
          "timeDate",
          "BPMNDiagram",
          "BPMNPlane",
          "conditionExpression",
          "multiInstanceLoopCharacteristics",
          "documentation"
          );

  private List<String> attributesForWhichSignavioGeneratesNewIds = Arrays.asList(
          "id",
          "messageRef",
          "errorRef",
          "bpmnElement",
          "dataStoreRef"
          );

  @Override
  public int differenceFound(Difference difference) {
    if (DifferenceConstants.ATTR_VALUE_ID == difference.getId()) {
      Attr attribute = (Attr) difference.getControlNodeDetail().getNode();
      String ownerElementName = attribute.getOwnerElement().getLocalName();
      String attributeName = attribute.getLocalName();
      if ("definitions".equals(ownerElementName) && "exporterVersion".equals(attributeName)) {
        // ignore different Signavio version number
        return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
      } else if (elementsForWhichSignavioGeneratesNewIds.contains(ownerElementName)
              && attributesForWhichSignavioGeneratesNewIds.contains(attributeName)
              && attribute.getValue().startsWith("sid-")) {
        return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
      }
    }
    return RETURN_ACCEPT_DIFFERENCE;
  }

  @Override
  public void skippedComparison(Node control, Node test) {
    throw new RuntimeException("DifferenceListener.skippedComparison: "
            + "unhandled control node type=" + control
            + ", unhandled test node type=" + test);
  }

}