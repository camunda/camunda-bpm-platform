package org.camunda.bpm.cycle.connector.test.util;

import java.io.InputStream;
import java.util.Scanner;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorNodeDTO;
import org.camunda.bpm.cycle.web.dto.RoundtripDTO;
import org.camunda.bpm.cycle.web.service.resource.RoundtripService;



public class TestHelper {

  public static final String TEST_RESOURCES_PATH = "src/test/resources/";
  public static final String MODEL_FOLDER = "models";
  public static final String LHS_MODELER = "LHS-Modeler";
  public static final String RHS_MODELER = "RHS-Modeler";
  
  private RoundtripService roundtripService;

  public TestHelper(RoundtripService roundtripService) {
    this.roundtripService = roundtripService;
  }
  
  /**
   * Creates a named roundtrip via the {@link RoundtripService} and returns the {@link RoundtripDTO}.  
   */
  public RoundtripDTO createTestRoundtrip() {
    RoundtripDTO roundtripDTO = new RoundtripDTO();
    roundtripDTO.setName("testRoundtrip");
    roundtripDTO = roundtripService.create(roundtripDTO);
    return roundtripDTO;
  }
  
  /**
   * Creates a named roundtrip with the given {@link ConnectorNode} as left hand side diagram and returns the {@link RoundtripDTO}.  
   */
  public RoundtripDTO createTestRoundtripWithLHS(ConnectorNode lhsConnectorNode) {
    RoundtripDTO roundtripDTO = createTestRoundtrip();
    roundtripDTO.setLeftHandSide(createTestBpmnDiagram(lhsConnectorNode, LHS_MODELER));
    roundtripDTO = roundtripService.updateDetails(roundtripDTO);
    return roundtripDTO;
  }
  
  /**
   * Creates a named roundtrip with the given {@link ConnectorNode} as right hand side diagram and returns the {@link RoundtripDTO}.  
   */
  public RoundtripDTO createTestRoundtripWithRHS(ConnectorNode rhsConnectorNode) {
    RoundtripDTO roundtripDTO = createTestRoundtrip();
    roundtripDTO.setRightHandSide(createTestBpmnDiagram(rhsConnectorNode, RHS_MODELER));
    roundtripDTO = roundtripService.updateDetails(roundtripDTO);
    return roundtripDTO;
  }
  
  /**
   * Creates a named roundtrip with the given {@link ConnectorNode}s as left/right hand side diagrams and returns the {@link RoundtripDTO}.  
   */
  public RoundtripDTO createTestRoundtripWithBothSides(ConnectorNode lhsConnectorNode, ConnectorNode rhsConnectorNode) {
    RoundtripDTO roundtripDTO = createTestRoundtrip();
    roundtripDTO.setLeftHandSide(createTestBpmnDiagram(lhsConnectorNode, LHS_MODELER));
    roundtripDTO.setRightHandSide(createTestBpmnDiagram(rhsConnectorNode, RHS_MODELER));
    roundtripDTO = roundtripService.updateDetails(roundtripDTO);
    return roundtripDTO;
  }
  
  /**
   * Creates a {@link BpmnDiagramDTO} with the given {@link ConnectorNode} and modeler label.
   */
  public static BpmnDiagramDTO createTestBpmnDiagram(ConnectorNode connectorNode, String modeler) {
    BpmnDiagramDTO bpmnDiagram = new BpmnDiagramDTO();
    bpmnDiagram.setModeler(modeler);
    ConnectorNodeDTO connectorNodeDTO = new ConnectorNodeDTO(connectorNode); 
    bpmnDiagram.setConnectorNode(connectorNodeDTO);
    return bpmnDiagram;
  }
  
  public static String readModelFromModelFolder(String modelName) {
    return new Scanner(TestHelper.class.getClass().getResourceAsStream("/" + MODEL_FOLDER + "/" + modelName), "UTF-8").useDelimiter("\\A").next();
  }
  
  /**
   * Creates a named {@link ConnectorNode} folder using the specified connector, which serves as a parent folder.  
   */
  public static ConnectorNode createConnectorNodeParentFolder(Connector connector, String name) {
    ConnectorNode connectorNodeParent = null;
    
    if (connector instanceof SignavioConnector) {
      connectorNodeParent = connector.createNode(((SignavioConnector) connector).getPrivateFolder().getId(), name, ConnectorNodeType.FOLDER, null);
    } else {
      connectorNodeParent = connector.createNode(connector.getRoot().getId(), name, ConnectorNodeType.FOLDER, null);
    }
    
    return connectorNodeParent;
  }
  
  /**
   * Creates a {@link ConnectorNode} file using the specified connector and connectorNodeParent as parent folder.  
   */
  public static ConnectorNode createConnectorNode(Connector connector, ConnectorNode connectorNodeParent, String model) throws Exception {
    ConnectorNode connectorNode = null;
    InputStream modelInputStream = null;
    
    try {
      if (model.endsWith(".sgx") && connector instanceof SignavioConnector) {
        connectorNode = ((SignavioConnector) connector).importSignavioArchive(connectorNodeParent, TEST_RESOURCES_PATH + model).get(0);
      } else {
        modelInputStream = IoUtil.readFileAsInputStream(model);
        String label = model.substring(model.lastIndexOf("/") + 1, model.length());
        connectorNode = connector.createNode(connectorNodeParent.getId(), label, ConnectorNodeType.BPMN_FILE, null);
        connector.updateContent(connectorNode, modelInputStream, null);
      }
      
      return connectorNode;
    } finally {
      IoUtil.closeSilently(modelInputStream);
    }
  }
}
