package com.camunda.fox.cycle.impl.connector.signavio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.api.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.RepositoryException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/test/signavio-connector-xml-config.xml"}
)
public class SignavioConnectorTest extends AbstractSignavioConnectorTest {

  @Before
  public void setUp() throws Exception {
    this.getSignavioConnector().init(this.getSignavioConnector().getConfiguration());
  }
  
  @Test
  public void testConfigurableViaXml() throws Exception {
    assertNotNull(this.getSignavioConnector());
    
    ConnectorConfiguration config = this.getSignavioConnector().getConfiguration();
    assertNotNull(config);
    
    assertTrue(config.getId() == 2);
    assertEquals("My SignavioConnector", config.getLabel());
    
    assertEquals(ConnectorLoginMode.GLOBAL, config.getLoginMode());
    assertEquals("test@camunda.com", config.getGlobalUser());
    assertEquals("testtest", config.getGlobalPassword()); // TODO: decrypt password!
    
    Map<String, String> prop = config.getProperties();
    assertNotNull(prop);
    
    assertEquals("http://vm2.camunda.com:8080", prop.get(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL));
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
    } catch (RepositoryException e) {
      // everything okay: we excepted an exception here
    }
  }
  
  @Test
  public void testGetChildren_Empty() {
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.createFolder(this.getSignavioConnector().getPrivateFolder(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      assertTrue(children.isEmpty());
      this.deleteFolder(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.deleteFolder(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneFolder() {
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.createFolder(this.getSignavioConnector().getPrivateFolder(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = this.createFolder(createdRootNode, "ChildFolder");
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      assertFalse(children.isEmpty());
      assertTrue(children.size() == 1);
      
      ConnectorNode child = children.get(0);
      assertEquals(newFolder, child);
      
      assertEquals("ChildFolder", child.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, child.getType());      
      
      this.deleteFolder(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.deleteFolder(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneModel() {
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.createFolder(this.getSignavioConnector().getPrivateFolder(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newModel = this.createEmptyModel(createdRootNode, "model1");
      assertEquals("model1", newModel.getLabel());
      assertEquals(ConnectorNodeType.FILE, newModel.getType());
      
      List<ConnectorNode> children = this.getSignavioConnector().getChildren(createdRootNode);
      
      assertFalse(children.isEmpty());
      assertTrue(children.size() == 1);
      
      ConnectorNode child = children.get(0);
      assertEquals(newModel, child);
      
      assertEquals("model1", child.getLabel());
      assertEquals(ConnectorNodeType.FILE, child.getType());
      
      this.deleteFolder(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.deleteFolder(createdRootNode);
      }
      fail("An exception has been thrown: " + e.getMessage());
    }
  }
  
  @Test
  public void testGetChildren_ContainingOneFolderAndOneModel() {
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.createFolder(this.getSignavioConnector().getPrivateFolder(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = this.createFolder(createdRootNode, "ChildFolder");
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      ConnectorNode newModel = this.createEmptyModel(createdRootNode, "model1");
      assertEquals("model1", newModel.getLabel());
      assertEquals(ConnectorNodeType.FILE, newModel.getType());
      
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
      assertEquals(ConnectorNodeType.FILE, childModel.getType());
      
      this.deleteFolder(createdRootNode);
    } catch (Exception e) {
      if (createdRootNode != null) {
        this.deleteFolder(createdRootNode);
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
  
}
