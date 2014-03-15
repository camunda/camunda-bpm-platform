package org.camunda.bpm.cycle.roundtrip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.util.ExceptionUtil;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Utility Class providing 
 * <ul>
 *   <li>replacement of bpmn element IDs with developer-friendly IDs</li>
 *   <li>extraction of an executable pool from a bpmn collaboration</li>
 * </ul> 
 * This class works on the xml representation of a bpmn process model provided as a String.
 * 
 * @author Daniel Meyer
 */
public class BpmnProcessModelUtil {
  
  public static final String UTF_8 = "UTF-8";
  
  private static final String MERGE_SOURCE_TECHNICAL_BPMN20_XML = "merge-source-technical.bpmn";
  private static final String MERGE_SOURCE_BUSINESS_BPMN20_XML = "merge-source-business.bpmn";
  private static final String MERGE_RESULT_BPMN20_XML = "merge-result.bpmn";
  
  private static final String BPMN_DI_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/DI";
  private static final String NAMESPACE_URI_BPMN_20 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  private static final String NAMESPACE_URI_BPMN_20_DI = "http://www.omg.org/spec/BPMN/20100524/DI";
  
  public static final String DEFAULT_ENGINEPOOL_NAME = "Process Engine";
  
  private static final String ID = "id";
  private static final String PROCESS = "process";
  private static final String PROCESS_REF = "processRef";
  private static final String PARTICIPANT = "participant";
  private static final String MESSAGE_FLOW = "messageFlow";
  private static final String SOURCE_REF = "sourceRef";
  private static final String TARGET_REF = "targetRef";
  private static final String BPMN_PLANE = "BPMNPlane";
  
  // XPATH expressions
  private static final String ENGINE_PROCESS_DETECTION_EXPR = "//bpmn:process[count(//bpmn:process) = 1 or @isExecutable = 'true' or @name = '%s'][1]";
  private static final String NAME_DETECTION_EXPR = "/@name";
  private static final String ID_DETECTION_EXPR = "/@id";
  private static final String ENGINE_POOL_DETECTION_EXPR = "//bpmn:participant[@processRef='%s' ]/@id";
  private static final String PARTICIPANT_DETECTION_EXPR = "//bpmn:participant[@processRef='%s']";
  private static final String X_COORD_BPMN_SHAPE_DETECTION_EXPR = "//bpmndi:BPMNShape[@bpmnElement='%s']/omgdc:Bounds/@x";
  private static final String Y_COORD_BPMN_SHAPE_DETECTION_EXPR = "//bpmndi:BPMNShape[@bpmnElement='%s']/omgdc:Bounds/@y";
  private static final String NAME_ATTR = "name";
  private static final String IS_EXECUTABLE_ATTR = "isExecutable";
  
  protected XsltTransformer transformer = XsltTransformer.instance();
  
  private static final Logger logger = Logger.getLogger(BpmnProcessModelUtil.class.getName());
  
  /**
   * Replaces the bpmn element IDs in a process model with developer friendly
   * IDs.
   * 
   * @param sourceModel
   *          a bpmn20 process model in XML representation
   * @param processEnginePoolId
   *          allows to provide a custom ID that is used for the first <process .../> element with 
   *          'isExecutable="true"' that is found by the transformer           
   * @return the process model such that the element ids are replaced with    
   *         developer-friendly IDs
   */
  public String replaceDeveloperFriendlyIds(String sourceModel, String processEnginePoolId) {
    
    ByteArrayInputStream inputStream = new ByteArrayInputStream(getBytesFromString(sourceModel));
    ByteArrayOutputStream resultModel = null;
    
    try {
      
      resultModel = transformer.developerFriendly(inputStream, processEnginePoolId, true);
      return getStringFromBytes(resultModel.toByteArray());
      
    } finally {
      IoUtil.closeSilently(inputStream);
      IoUtil.closeSilently(resultModel);
    }
    
  }

  /**
   * Replaces the bpmn element IDs in a process model with developer friendly
   * IDs.
   * 
   * @param sourceModel
   *          a bpmn20 process model in XML representation
   * @return the process model such that the element ids are replaced with    
   *         developer-friendly IDs
   */
  public String replaceDeveloperFriendlyIds(String sourceModel) {    
    return replaceDeveloperFriendlyIds(sourceModel, "Process_Engine");
  }

  /**
   * Extract executable pool based on string data
   *
   * @param sourceModel
   * @return
   *
   * @see #extractExecutablePool(java.io.InputStream)
   */
  public String extractExecutablePool(String sourceModel) {

    InputStream input = new ByteArrayInputStream(getBytesFromString(sourceModel));

    InputStream result = extractExecutablePool(input);

    return getStringFromBytes(IoUtil.readInputStream(result, "foo"));
  }

  /**
   * Takes a bpmn process model in XML representation as input. Removes all pools 
   * except for a single executable pool (property 'isExecutable="true").
   * 
   * NOTE: assumes that the process model contains a single executable pool.
   * 
   * @param sourceModel
   *         a bpmn20 process model in XML representation
   * @return the process model containing the extracted pool
   * 
   */
  public InputStream extractExecutablePool(InputStream sourceModel) {
    ByteArrayOutputStream resultModel = null;
    
    try {
      
      resultModel = transformer.poolExtraction(sourceModel, true);
      return new ByteArrayInputStream(resultModel.toByteArray());
    } finally {
      IoUtil.closeSilently(sourceModel);
      IoUtil.closeSilently(resultModel);
    }
    
  }
  
  /**
   * Import the changes from a source bpmn process model to a target bpmn process model.
   */
  public String importChangesFromExecutableBpmnModel(String sourceModel, String targetModel) {
    logger.info("Starting to merge bpmn process models.");
    
    IoUtil.writeStringToFileIfDebug(sourceModel, "source_model", MERGE_SOURCE_TECHNICAL_BPMN20_XML);
    IoUtil.writeStringToFileIfDebug(targetModel, "target_model", MERGE_SOURCE_BUSINESS_BPMN20_XML);

    String defaultEngineProcessExpression = null;
    String engineProcessName = null;
    String engineProcessId = null;
    try {
      defaultEngineProcessExpression = String.format(ENGINE_PROCESS_DETECTION_EXPR, DEFAULT_ENGINEPOOL_NAME);
      engineProcessName = XmlUtil.getXPathResult(defaultEngineProcessExpression + NAME_DETECTION_EXPR, sourceModel);
      engineProcessId = XmlUtil.getXPathResult(defaultEngineProcessExpression + ID_DETECTION_EXPR, sourceModel);
    } catch (Exception e) {
      throw new CycleException("Failure in source model: " + ExceptionUtil.getRootCause(e).getMessage(), e);
    }
      
    String engineProcessIdInBusinessModel = null;
    try {
      engineProcessIdInBusinessModel = XmlUtil.getXPathResult(String.format(ENGINE_PROCESS_DETECTION_EXPR, engineProcessName) + ID_DETECTION_EXPR, targetModel);
    } catch (Exception e) {
      throw new CycleException("Failure in target model: " + ExceptionUtil.getRootCause(e).getMessage(), e);
    }
    
    if (engineProcessIdInBusinessModel == null || (engineProcessIdInBusinessModel != null && engineProcessIdInBusinessModel.isEmpty())){
      throw new CycleException("Could not detect an engine pool. Please make sure that the 'isExecutable' attribute is set, or that the engine pool name matches in technical and business model.");
    }
    String enginePoolDetectionExpression = null;
    String enginePoolIdInBusinessModel = null;
    try {
      enginePoolDetectionExpression = String.format(ENGINE_POOL_DETECTION_EXPR, engineProcessIdInBusinessModel);
      enginePoolIdInBusinessModel = XmlUtil.getXPathResult(enginePoolDetectionExpression, targetModel);
      
    } catch (Exception e) {
      throw new CycleException("Failure in target model: " + ExceptionUtil.getRootCause(e), e);
    }
    
    // if id changed, replace in business model
    if (engineProcessName != null && !engineProcessName.isEmpty() && 
        engineProcessIdInBusinessModel != null && !engineProcessId.equals(engineProcessIdInBusinessModel) &&
        !engineProcessIdInBusinessModel.isEmpty()) {
      targetModel = targetModel.replaceAll(engineProcessIdInBusinessModel, engineProcessId);
    }
    
    String offsetX = "0.0";
    String offsetY = "0.0";
    
    try {
      offsetX = "-"+ XmlUtil.getXPathResult(String.format(X_COORD_BPMN_SHAPE_DETECTION_EXPR, enginePoolIdInBusinessModel), targetModel);
      offsetY=  "-"+ XmlUtil.getXPathResult(String.format(Y_COORD_BPMN_SHAPE_DETECTION_EXPR, enginePoolIdInBusinessModel), targetModel);
    } catch (Exception e) {
      logger.log(Level.FINE, "Could not get offset for engine pool", e);
    }
    
    Document businessModel = null;
    Document technicalModel = null;
    
    businessModel = this.getDocumentFromXmlString(targetModel);
    
    // update participant name in business model
    try{
      Element participant = (Element) XmlUtil.getSingleElementByXPath(businessModel, String.format(PARTICIPANT_DETECTION_EXPR,engineProcessId));
      if (participant != null) {
        participant.setAttribute(NAME_ATTR, engineProcessName);
      }
    }
    catch(Exception e){
      logger.log(Level.FINE, "Could not update participant name", e);
    }

    try { 
      // do a pool extraction on technical model to update DI offset
      String techXml = XsltTransformer.instance()
              .poolExtraction(new ByteArrayInputStream(sourceModel.getBytes(UTF_8)), true, offsetX, offsetY)
              .toString(UTF_8);
      technicalModel = this.getDocumentFromXmlString(techXml);
    } catch (Exception e) {
      throw new CycleException("Error while parsing the source model, which is to be imported.", e);
    }
    
    NodeList processes = businessModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, PROCESS);
    NodeList participants = businessModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, PARTICIPANT);
    
    String mergedBpmn20XmlContent;
    
    if (processes.getLength() < 2 && participants.getLength() < 2) {
      // no merge required
      mergedBpmn20XmlContent = sourceModel;
    } else {
      // locate engine pool => get id
      // this search could be avoided by fixing HEMERA-1057
      Element engineProcess = (Element) this.getElementById(businessModel, engineProcessId);
      engineProcess.setAttribute(NAME_ATTR, engineProcessName);
      engineProcess.setAttribute(IS_EXECUTABLE_ATTR, "true");
        
      // Reconnect Message Flows to Participant if their source or target FlowNode does no longer exist and loop over all Message Flows 
      try {
        redirectMessageFlows(businessModel, technicalModel, engineProcess);
      } catch(Exception e) {
        throw new CycleException("Could not update messageflow references and diagram info", e);
      }
      
      // exchange process of engine pool and remove all children of engine pool process
      NodeList childNodes = engineProcess.getChildNodes();
      this.removeChildrenFromEngineProcess(childNodes);

      // remove DI Shapes and Edges that do not have model elements associated to them
      this.removeBpmnDiElementsThatReferenceNonExistingBpmnElements(businessModel, engineProcessId);
        
      // add children of technical process to engine process
      List<String> skipped = addTechnicalChildrenToEngineProcess(businessModel, technicalModel, engineProcess, childNodes);

      // add all DI Shapes and Edges of the technical model
      this.addTechnicalDIToBusiness(businessModel, technicalModel, skipped);
      
      try { 
        // fix refs
        this.checkTechnicalReferences(businessModel, technicalModel);
      } catch (Exception e) {
        throw new RuntimeException("Could not update references for all root elements", e);
      }

      // convert DOM Document to String  
      mergedBpmn20XmlContent = convertNodeToXmlString(businessModel);
    }
    
    IoUtil.writeStringToFileIfDebug(mergedBpmn20XmlContent, "result_model", MERGE_RESULT_BPMN20_XML);
    
    logger.info("Finishing to merge bpmn process models.");
    
    return mergedBpmn20XmlContent;
  }
  
  private void checkTechnicalReferences(Document businessModel, Document technicalModel) {
    String [] refTypes = {"message", "signal", "error", "dataStore"};
    Node businessDefinitions = (Node) businessModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, "definitions").item(0);
    
    for (String refType : refTypes) {
      NodeList techElems = technicalModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, refType);
      for (int techElemIndex = 0; techElemIndex < techElems.getLength(); techElemIndex++) {
        Element techElem = (Element) techElems.item(techElemIndex);
        Element businessElem = businessModel.getElementById(techElem.getAttribute("id"));
        if (businessElem == null) {

          if (techElem.getAttribute("name") != null && !techElem.getAttribute("name").isEmpty()){
            try {
              businessElem = (Element) getElementByAttribute(businessModel, "bpmn:"+refType, "name", techElem.getAttribute("name"));
            }
            catch (Exception e) {
              logger.log(Level.FINE, "Could not correlate message element, inserting element from tech. model");
            }
          }
          
          if (businessElem == null) {
            // add elem
            businessDefinitions.insertBefore(businessModel.importNode(techElem, true), businessDefinitions.getFirstChild());
          }
          else{
            // update id
            businessElem.setAttribute("id", techElem.getAttribute("id"));
          }
        }
        else{
          // everything ok for now, we might merge attributes in the future
        }
      }
    }
  }
  
  private void addTechnicalDIToBusiness(Document businessModel, Document technicalModel, List<String> skipped) {
    NodeList technichalBPMNPlanes = technicalModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20_DI, BPMN_PLANE);
    Element technichalBPMNPlane;
    if (technichalBPMNPlanes.getLength() != 1) {
      throw new CycleException("The technical model to be imported must contain exactly one BPMNPlane.");
    } else {
      technichalBPMNPlane = (Element) technichalBPMNPlanes.item(0);
    }
    NodeList businessBPMNPlanes = businessModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20_DI, BPMN_PLANE);
    Element businessBPMNPlane;
    if (businessBPMNPlanes.getLength() != 1) {
      throw new CycleException("The business model to be updated must contain exactly one BPMNPlane.");
    } else {
      businessBPMNPlane = (Element) businessBPMNPlanes.item(0);
    }
    NodeList technichalBpmnDiElements = technichalBPMNPlane.getChildNodes();
    for (int i = 0; i < technichalBpmnDiElements.getLength(); i++) {
      Node childNode = technichalBpmnDiElements.item(i);
      String bpmnElement = getAttribute(childNode, "bpmnElement");
      
      if(bpmnElement == null || (bpmnElement != null && !skipped.contains(bpmnElement)) ) {
        businessBPMNPlane.appendChild(businessModel.importNode(childNode, true));
      }
    }
  }
  
  private List<String> addTechnicalChildrenToEngineProcess(Document businessModel, Document technicalModel, Element engineProcess, NodeList childNodes) {
    NodeList technicalProcesses = technicalModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, PROCESS);
    List<String> skipped = new ArrayList<String>();
    
    if (technicalProcesses.getLength() != 1) {
      throw new CycleException("The technical model to be imported must contain exactly one process.");
    } else {
      Element technicalProcess = (Element) technicalProcesses.item(0);
      childNodes = technicalProcess.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node childNode = childNodes.item(i);
        String childNodeId = this.getId(childNode);
        
        if (childNodeId == null || (childNodeId!= null && getElementById(businessModel, childNodeId) == null) ){
          engineProcess.appendChild(businessModel.importNode(childNode, true));
        } else if (childNodeId != null) {
          skipped.add(childNodeId);
          String warning = String.format("Element with ID %s already exists in business model, skipping import of this element", childNodeId);
          logger.log(Level.WARNING, warning);
        }
      }
    }
    
    return skipped;
  }
  
  private String getId(Node node){
    return this.getAttribute(node, "id");
  }
  
  private String getAttribute(Node node, String attribute){
    if (node instanceof Element) {
      Element element = (Element) node;
      return element.getAttribute(attribute);
    }
    return null;
  }
  
  private void removeChildrenFromEngineProcess(NodeList childNodes) {
    List<Node> nodesToRemove = new ArrayList<Node>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      nodesToRemove.add(childNode);
    }
    for (Node node : nodesToRemove) {
      node.getParentNode().removeChild(node);
    }
  }
  
  private void redirectMessageFlows(Document businessModel, Document technicalModel, Element engineProcess) {
    NodeList messageFlows = businessModel.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, MESSAGE_FLOW);
    Document engineProcessDocument = getDocumentFromXmlString(this.convertNodeToXmlString(engineProcess));
    
    for (int i = 0; i < messageFlows.getLength(); i++) {
      Element messageFlow = (Element) messageFlows.item(i);
      // check that source exists
      String sourceRef = messageFlow.getAttribute(SOURCE_REF);
      String targetRef = messageFlow.getAttribute(TARGET_REF);
      String engineParticpantId = this.getParticipantOfProcess(businessModel, engineProcess).getAttribute(ID);

      
      // compare old with new engine process add reconnect if reference is deleted
      if (getElementById(engineProcessDocument, sourceRef) != null && getElementById(technicalModel, sourceRef) == null){
        messageFlow.setAttribute(SOURCE_REF, engineParticpantId);
        this.updateMessageFlowDISource(businessModel, messageFlow, engineParticpantId);
      }
      
      // compare old with new engine process add reconnect if reference is deleted
      if (getElementById(engineProcessDocument, targetRef) != null && getElementById(technicalModel, targetRef) == null){
        messageFlow.setAttribute(TARGET_REF, engineParticpantId);
        this.updateMessageFlowDITarget(businessModel, messageFlow, engineParticpantId);
      }
    }
  }
  
  private void updateMessageFlowDISource(Document businessModel, Element messageFlow, String engineParticpantId) {
    // get for first/source DI waypoint
    // and update target waypoint with x of source waypoint and y = enginePoolPosition + height
    
    Element waypoint = (Element) XmlUtil.getSingleElementByXPath(businessModel, String.format("//bpmndi:BPMNEdge[@bpmnElement='%s']/omgdi:waypoint[1]", messageFlow.getAttribute(ID)));
    
    if (waypoint != null){
       
       double targetY = Double.valueOf(waypoint.getAttribute("y"));
       
       Bounds engineBounds = this.getDIBoundsForBpmnElement(businessModel, engineParticpantId);
       
       double sourceY = 0.0f;
       //   from which direction are we reconnecting?
       if (targetY <= engineBounds.y){
         sourceY = engineBounds.y;
       }else{
         sourceY = engineBounds.y + engineBounds.height;
       }
       
       waypoint.getAttributes().getNamedItem("y").setTextContent(Double.toString(sourceY));
    }
  }
  
  private void updateMessageFlowDITarget(Document businessModel, Element messageFlow, String engineParticpantId) {
    // get for first/source DI waypoint
    // and update target waypoint with x of source waypoint and y = enginePoolPosition + height
    
    Element waypoint = (Element) XmlUtil.getSingleElementByXPath(businessModel, String.format("//bpmndi:BPMNEdge[@bpmnElement='%s']/omgdi:waypoint[last()]", messageFlow.getAttribute(ID)));
    
    if (waypoint != null){
       
       double sourceY = Double.valueOf(waypoint.getAttribute("y"));
       
       Bounds engineBounds = this.getDIBoundsForBpmnElement(businessModel, engineParticpantId);
       
       double targetY = 0.0;
       
       // from which direction are we reconnecting?
       if (sourceY <= engineBounds.y){
         targetY = engineBounds.y;
       }else{
         targetY = engineBounds.y + engineBounds.height;
       }
       
       waypoint.getAttributes().getNamedItem("y").setTextContent(Double.toString(targetY));
    }
  }
  
  private Bounds getDIBoundsForBpmnElement(Document document, String bpmnElementId){
    Element bounds = (Element) XmlUtil.getSingleElementByXPath(document, String.format("//bpmndi:BPMNShape[@bpmnElement='%s']/omgdc:Bounds", bpmnElementId));
    if (bounds != null){
      return new Bounds(Double.valueOf(bounds.getAttribute("x")),
                        Double.valueOf(bounds.getAttribute("y")), 
                        Double.valueOf(bounds.getAttribute("width")), 
                        Double.valueOf(bounds.getAttribute("height")));
    }
    return null;
  }

  
  private Element getParticipantOfProcess(Document model, Element process) {
    String processId = process.getAttribute(ID);
    NodeList participants = model.getElementsByTagNameNS(NAMESPACE_URI_BPMN_20, PARTICIPANT);
    for (int j = 0; j < participants.getLength(); j++) {
      Element participant = (Element) participants.item(j);
      if (participant.getAttribute(PROCESS_REF).equals(processId)) {
        return participant;
      }
    }
    return null;
  }
  
  private String convertNodeToXmlString(Node node){
    try {
      StringWriter stringWriter = new StringWriter();
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
      return stringWriter.getBuffer().toString();
    } catch (Exception e) {
      throw new CycleException("Error while serializing the updated model.", e);
    }
  }
  
  private Object getElementById(Document engineProcessDocument, String idValue) {
    return this.getElementByAttribute(engineProcessDocument, "*", "id", idValue);
  }
  
  private Object getElementByAttribute(Document engineProcessDocument, String element, String attribute, String value) {
    return XmlUtil.getSingleElementByXPath(engineProcessDocument, String.format("//%s[@%s='%s']", element, attribute, value));
  }
  
  private Document getDocumentFromXmlString(String xml){
    // Initiate DocumentBuilderFactory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // To get one that understands namespaces
    factory.setNamespaceAware(true);
    
    DocumentBuilder builder;
    try {
      // Get DocumentBuilder
      builder = factory.newDocumentBuilder();
      // Parse and load into memory the Document
      return builder.parse(new ByteArrayInputStream(xml.getBytes(UTF_8)));
    } catch (Exception e) {
      throw new CycleException("Error while parsing the xml into document '" , e);
    }
  }
  
  /**
   * removes bpmndi-shapes which reference elements which are not present in the
   * bpmn-model
   * <p />
   * bit hacky...
   *
   * @author daniel.meyer@camunda.com
   * @author Falko Menge
   */
  private void removeBpmnDiElementsThatReferenceNonExistingBpmnElements(Document document, String engineProcessId) {
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
  
  
  // utils ////////////////////////////////////////////////////
  
  protected byte[] getBytesFromString(String string) {
    try {
      return string.getBytes(UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new CycleException("Unable to get bytes from source model", e);
    }
  }

  protected String getStringFromBytes(byte[] byteArray) {
    try {
      return new String(byteArray, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new CycleException("Unable to get bytes from result model", e);
    }
  }

  private static class Bounds {
    double x;
    double y;
    double width;
    double height;
    
    public Bounds(double x, double y, double width, double height) {
      super();
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
    
    @Override
    public String toString() {
      return "Bounds [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }
  }
  
}
