package com.camunda.fox.cycle.connector.signavio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.CycleException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test/signavio-connector-xml-config.xml"}
)
public class SignavioConnectorTest {

  @Inject
  private SignavioConnector signavioConnector;
  
  @Before
  public void setUp() throws Exception {
    this.getSignavioConnector().init(this.getSignavioConnector().getConfiguration());
    if (this.getSignavioConnector().needsLogin()) {
      this.getSignavioConnector().login(this.getSignavioConnector().getConfiguration().getGlobalUser(), this.getSignavioConnector().getConfiguration().getGlobalPassword());
    }
  }
  
  @After
  public void tearDown() throws Exception {
    this.getSignavioConnector().dispose();
  }
  
  @Test
  public void testConfigurableViaXml() throws Exception {
    assertNotNull(this.getSignavioConnector());
    
    ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
    assertNotNull(config);
    
    assertTrue(config.getId() == 2);
    assertEquals("My SignavioConnector", config.getName());
    
    Map<String, String> prop = config.getProperties();
    assertNotNull(prop);
    
    assertNotNull(config.getLoginMode());
    assertNotNull(config.getGlobalUser());
    assertNotNull(config.getGlobalPassword());
    assertNotNull(prop.get(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL));
    
    // TODO: nre: cannot do this if credentials are configurable
    // assertEquals(ConnectorLoginMode.GLOBAL, config.getLoginMode());
    // assertEquals("test@camunda.com", config.getGlobalUser());
    // assertEquals("testtest", config.getGlobalPassword()); // TODO: decrypt password!
    
    // assertEquals("http://vm2.camunda.com:8080", prop.get(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL));
  }
  
  @Test
  public void testLogin() {
    try {
      ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
      this.getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    } catch (Exception e) {
      fail("Something went wrong: Login failed with following exception: " + e.getMessage());
    }
  }
  
  @Test
  public void testFailedLogin() {
    try {
      ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
      this.getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword() + "1");
      fail("Something went wrong: An exception had to be thrown due to failing login.");
    } catch (CycleException e) {
      // everything okay: we excepted an exception here
    }
  }
  
  @Test
  public void testGetChildren_Empty() {
    ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
    this.getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.getSignavioConnector().createNode(this.getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      assertTrue(children.isEmpty());
      this.getSignavioConnector().deleteNode(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.getSignavioConnector().deleteNode(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneFolder() {
    ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
    this.getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.getSignavioConnector().createNode(this.getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = this.getSignavioConnector().createNode(createdRootNode.getId(), "ChildFolder", ConnectorNodeType.FOLDER);
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      assertFalse(children.isEmpty());
      assertTrue(children.size() == 1);
      
      ConnectorNode child = children.get(0);
      assertEquals(newFolder, child);
      
      assertEquals("ChildFolder", child.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, child.getType());      
      
      this.getSignavioConnector().deleteNode(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.getSignavioConnector().deleteNode(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneModel() {
    ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
    this.getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.getSignavioConnector().createNode(this.getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newModel = this.getSignavioConnector().createNode(createdRootNode.getId(), "model1", ConnectorNodeType.BPMN_FILE);
      assertEquals("model1", newModel.getLabel());
      assertEquals(ConnectorNodeType.BPMN_FILE, newModel.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      
      assertEquals(1, children.size());
      
      ConnectorNode child = children.get(0);
      assertEquals(newModel, child);
      
      assertEquals("model1", child.getLabel());
      assertEquals(ConnectorNodeType.BPMN_FILE, child.getType());
      
      this.getSignavioConnector().deleteNode(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.getSignavioConnector().deleteNode(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneFolderAndOneModel() {
    ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
    this.getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.getSignavioConnector().createNode(this.getSignavioConnector().getPrivateFolder().getId(), "TestFolder", ConnectorNodeType.FOLDER);
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = this.getSignavioConnector().createNode(createdRootNode.getId(), "ChildFolder", ConnectorNodeType.FOLDER);
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      ConnectorNode newModel = this.getSignavioConnector().createNode(createdRootNode.getId(), "model1", ConnectorNodeType.BPMN_FILE);
      assertEquals("model1", newModel.getLabel());
      assertEquals(ConnectorNodeType.BPMN_FILE, newModel.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      
      assertFalse(children.isEmpty());
      assertTrue(children.size() == 2);
      
      ConnectorNode childFolder = children.get(0); // You get folders at first!
      assertEquals(newFolder, childFolder);
      
      assertEquals("ChildFolder", childFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, childFolder.getType());    
      
      
      ConnectorNode childModel = children.get(1);
      assertEquals(newModel, childModel);
      
      assertEquals("model1", childModel.getLabel());
      assertEquals(ConnectorNodeType.BPMN_FILE, childModel.getType());
      
      this.getSignavioConnector().deleteNode(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.getSignavioConnector().deleteNode(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetRoot() throws Exception {
    ConnectorNode root = this.getSignavioConnector().getRoot();
    assertEquals("/", root.getId());
    assertEquals("/", root.getLabel());
    assertEquals(ConnectorNodeType.FOLDER, root.getType());
  }
  
  @Test
  public void testDispose() {
    this.getSignavioConnector().dispose();
    assertTrue(this.getSignavioConnector().needsLogin());
    try {
      this.getSignavioConnector().getPrivateFolder();
      fail("Something went wrong: An exception had to be thrown due to the client has diposed before.");
    } catch (Exception e) {
      // everything okay: we excepted an exception here
    }
    
  }
  
  private SignavioConnector getSignavioConnector() {
    return this.signavioConnector;
  }
  
}
