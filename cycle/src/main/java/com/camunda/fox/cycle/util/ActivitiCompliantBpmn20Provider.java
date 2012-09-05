package com.camunda.fox.cycle.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Bernd Ruecker
 * @author daniel.meyer@camunda.com
 * @author Falko Menge
 * @author Florian Beckmann
 * @author Andreas Drobisch
 */
public class ActivitiCompliantBpmn20Provider {
  
  /**
   * Controls if intermediate results are written to files.
   */
  public static boolean debug = false;

  /**
   * Directory, into which intermediate results are written.
   */
  public static String debugDir;
  
  private static final String BPMN_DI_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/DI";
  private static Logger log = Logger.getLogger(ActivitiCompliantBpmn20Provider.class.getSimpleName());
  
  public static void removeBpmnDiElementsThatReferenceNonExistingBpmnElements(Document document, String engineProcessId) {
    List<Element> elementsToRemove = findBpmnDiElementsThatReferenceNonExistingBpmnElements(document, "BPMNShape", engineProcessId);
    elementsToRemove.addAll(findBpmnDiElementsThatReferenceNonExistingBpmnElements(document, "BPMNEdge", engineProcessId));

    for (Element element : elementsToRemove) {
      element.getParentNode().removeChild(element);
    }
  }

  private static List<Element> findBpmnDiElementsThatReferenceNonExistingBpmnElements(Document document, String localName, String engineProcessId) {
    List<Element> elementsToRemove = new ArrayList<Element>();
    NodeList bpmnShapes = document.getElementsByTagNameNS(BPMN_DI_NAMESPACE, localName);
    for (int i = 0; i < bpmnShapes.getLength(); i++) {
      Element currentShape = (Element) bpmnShapes.item(i);
      String referencedElementId = currentShape.getAttribute("bpmnElement");

      String xPathIdResult = XmlUtil.getXPathResult(String.format("count(//*[@id=\"%s\" and namespace-uri() != '" + BPMN_DI_NAMESPACE + "'])",referencedElementId), document);
      
      if (engineProcessId != null){
        Boolean removeEnginePoolRef = new Boolean(XmlUtil.getXPathResult(String.format("boolean(//bpmn:participant[@id='%s' and @processRef='%s'])" ,referencedElementId, engineProcessId), document));
        
        if (removeEnginePoolRef){
        	elementsToRemove.add(currentShape);
        }
      }

      int idCount = new Integer(xPathIdResult);

      if (idCount == 0) {
        elementsToRemove.add(currentShape);
      }
    }
    return elementsToRemove;
  }

//  /**
//   * Writes intermediate result to file for debugging purposes.
//   */
//  public static void writeStringToFileIfDebug(RepositoryArtifact artifact, String data, String suffix) {
//    writeStringToFileIfDebug(artifact.getMetadata().getName(), data, suffix);
//  }
//  
//  public static void writeStringToFileIfDebug(String artifactName, String data, String suffix) {
//    if (debug) {
//      String dir = "";
//      if (debugDir != null && debugDir.length() > 0) {
//        dir = debugDir + System.getProperty("file.separator");
//        File debugDirectory = new File(dir);
//        if (!debugDirectory.exists()) {
//          if (!debugDirectory.mkdirs()) {
//            throw new RuntimeException("Unable to create debugDirectory: " + debugDirectory.getAbsolutePath());
//          }
//        }
//      }
//      String fileName = dir + artifactName + "." + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS_").format(new Date()) + suffix;
//      try {
//        FileWriter writer = new FileWriter(fileName);
//        writer.write(data);
//        writer.flush();
//        writer.close();
//      } catch (IOException e) {
//        throw new RuntimeException("Unable to write debug file: " + fileName, e);
//      }
//    }
//  }

}
