package org.camunda.bpm.cycle.connector.signavio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test/signavio-connector-xml-config.xml"}
)
public class SignavioConnectorTest {

  @Inject
  private SignavioConnector signavioConnector;
  
  @Before
  public void setUp() throws Exception {
    getSignavioConnector().init(getSignavioConnector().getConfiguration());
    if (getSignavioConnector().needsLogin()) {
      getSignavioConnector().login(getSignavioConnector().getConfiguration().getGlobalUser(), getSignavioConnector().getConfiguration().getGlobalPassword());
    }
  }
  
  @After
  public void tearDown() throws Exception {
    getSignavioConnector().dispose();
  }
  
  @Test
  public void testConfigurableViaXml() throws Exception {
    assertNotNull(getSignavioConnector());
    
    ConnectorConfiguration config = getSignavioConnector().getConfiguration();
    assertNotNull(config);
    
    assertTrue(config.getId() == 2);
    assertEquals("My SignavioConnector", config.getName());
    
    Map<String, String> prop = config.getProperties();
    assertNotNull(prop);
    
    assertNotNull(config.getLoginMode());
    assertNotNull(config.getGlobalUser());
    assertNotNull(config.getGlobalPassword());
  }
  
  @Test
  public void testLogin() {
    try {
      ConnectorConfiguration config = getSignavioConnector().getConfiguration();
      getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    } catch (Exception e) {
      fail("Something went wrong: Login failed with following exception: " + e.getMessage());
    }
  }
  
  @Test
  public void testFailedLogin() {
    try {
      ConnectorConfiguration config = getSignavioConnector().getConfiguration();
      getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword() + "1");
      fail("Something went wrong: An exception had to be thrown due to failing login.");
    } catch (CycleException e) {
      // everything okay: we excepted an exception here
    }
  }
  
  @Test
  public void testGetChildren_Empty() {
    ConnectorConfiguration config = getSignavioConnector().getConfiguration();
    getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = getSignavioConnector().createNode(getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER, null);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      List<ConnectorNode> children = getSignavioConnector().getChildren(createdRootNode);
      assertTrue(children.isEmpty());
      getSignavioConnector().deleteNode(createdRootNode, null);
    } catch (Exception e) {
      if (createdRootNode != null) {
        getSignavioConnector().deleteNode(createdRootNode,  null);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneFolder() {
    ConnectorConfiguration config = getSignavioConnector().getConfiguration();
    getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = getSignavioConnector().createNode(getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER, null);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = getSignavioConnector().createNode(createdRootNode.getId(), "ChildFolder", ConnectorNodeType.FOLDER, null);
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      List<ConnectorNode> children = getSignavioConnector().getChildren(createdRootNode);
      assertFalse(children.isEmpty());
      assertTrue(children.size() == 1);
      
      ConnectorNode child = children.get(0);
      assertEquals(newFolder, child);
      
      assertEquals("ChildFolder", child.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, child.getType());      
      
      getSignavioConnector().deleteNode(createdRootNode, null);
    } catch (Exception e) {
      if (createdRootNode != null) {
        getSignavioConnector().deleteNode(createdRootNode, null);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneModel() {
    ConnectorConfiguration config = getSignavioConnector().getConfiguration();
    getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = getSignavioConnector().createNode(getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER, null);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newModel = getSignavioConnector().createNode(createdRootNode.getId(), "model1", ConnectorNodeType.BPMN_FILE, "create model1");
      assertEquals("model1", newModel.getLabel());
      assertEquals("create model1", newModel.getMessage());
      assertEquals(ConnectorNodeType.BPMN_FILE, newModel.getType());
      
      List<ConnectorNode> children = getSignavioConnector().getChildren(createdRootNode);
      
      assertEquals(1, children.size());
      
      ConnectorNode child = children.get(0);
      assertEquals(newModel, child);
      
      assertEquals("model1", child.getLabel());
      assertEquals(ConnectorNodeType.BPMN_FILE, child.getType());
      
      getSignavioConnector().deleteNode(createdRootNode, null);
    } catch (Exception e) {
      if (createdRootNode != null) {
        getSignavioConnector().deleteNode(createdRootNode, null);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneFolderAndOneModel() throws Exception {
    ConnectorConfiguration config = getSignavioConnector().getConfiguration();
    getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = getSignavioConnector().createNode(getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER, null);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = getSignavioConnector().createNode(createdRootNode.getId(), "ChildFolder", ConnectorNodeType.FOLDER, null);
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      ConnectorNode newModel = getSignavioConnector().createNode(createdRootNode.getId(), "model1", ConnectorNodeType.BPMN_FILE, "create model1");
      assertEquals("model1", newModel.getLabel());
      assertEquals("create model1", newModel.getMessage());
      assertEquals(ConnectorNodeType.BPMN_FILE, newModel.getType());
      
      List<ConnectorNode> children = getSignavioConnector().getChildren(createdRootNode);
      
      assertFalse(children.isEmpty());
      assertTrue(children.size() == 2);
      
      ConnectorNode childFolder = children.get(0); // You get folders at first!
      assertEquals(newFolder, childFolder);
      
      assertEquals("ChildFolder", childFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, childFolder.getType());    
      
      
      ConnectorNode childModel = children.get(1);
      assertEquals(newModel, childModel);
      
      assertEquals("model1", childModel.getLabel());
      assertEquals("create model1", childModel.getMessage());
      assertEquals(ConnectorNodeType.BPMN_FILE, childModel.getType());
      
      getSignavioConnector().deleteNode(createdRootNode, null);
    } catch (Exception e) {
      if (createdRootNode != null) {
        getSignavioConnector().deleteNode(createdRootNode, null);
      }
      
      throw e;
    }
  }
  
  @Test
  public void testGetRoot() throws Exception {
    ConnectorNode root = getSignavioConnector().getRoot();
    assertEquals("/", root.getId());
    assertEquals("/", root.getLabel());
    assertEquals(ConnectorNodeType.FOLDER, root.getType());
  }
  
  @Test
  public void testDispose() {
    getSignavioConnector().dispose();
    assertTrue(getSignavioConnector().needsLogin());
    try {
      getSignavioConnector().getPrivateFolder();
      fail("Something went wrong: An exception had to be thrown due to the client has disposed before.");
    } catch (Exception e) {
      // everything okay: we excepted an exception here
    }
    
  }
  
  private SignavioConnector getSignavioConnector() {
    return signavioConnector;
  }
  
}
