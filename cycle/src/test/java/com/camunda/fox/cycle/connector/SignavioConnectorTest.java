package com.camunda.fox.cycle.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
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
import com.camunda.fox.cycle.impl.connector.signavio.SignavioConnector;
import com.camunda.fox.cycle.impl.connector.signavio.SignavioLoginForm;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/test/signavio-connector-xml-config.xml"}
)
public class SignavioConnectorTest {

  private static final String SIGNAVIO_PUBLIC_FOLDER_ID = "/3fe94c4502954d01a278370ac71e7ef7";
  
  @Inject
  private SignavioConnector signavioConnector;
  
  private SignavioTestHelperClient signavioClient;
  private boolean loggedIn;
  
  @Test
  public void testConfigurableViaXml() throws Exception {
    assertNotNull(this.signavioConnector);
    
    ConnectorConfiguration config = this.signavioConnector.getConfiguration();
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
      ConnectorConfiguration config = this.signavioConnector.getConfiguration();
      this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword());
    } catch (Exception e) {
      fail("Something went wrong: Login failed with following exception: " + e.getMessage());
    }
  }
  
  @Test
  public void testFailedLogin() {
    try {
      ConnectorConfiguration config = this.signavioConnector.getConfiguration();
      this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword() + "1");
      fail("Something went wrong: An exception had to be thrown due to failing login.");
    } catch (RepositoryException e) {
      // everything okay: we excepted an exception here
    }
  }
  
  @Test
  public void testGetChildren_Empty() {
    ConnectorNode createdRootNode = null;
    try {
      createdRootNode = this.createFolder(this.createPublicConnectorNode(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      List<ConnectorNode> children = this.signavioConnector.getChildren(createdRootNode);
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
      createdRootNode = this.createFolder(this.createPublicConnectorNode(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = this.createFolder(createdRootNode, "ChildFolder");
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      List<ConnectorNode> children = this.signavioConnector.getChildren(createdRootNode);
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
      createdRootNode = this.createFolder(this.createPublicConnectorNode(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newModel = this.createModel(createdRootNode, "model1");
      assertEquals("model1", newModel.getLabel());
      assertEquals(ConnectorNodeType.FILE, newModel.getType());
      
      List<ConnectorNode> children = this.signavioConnector.getChildren(createdRootNode);
      
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
      createdRootNode = this.createFolder(this.createPublicConnectorNode(), "TestFolder");
      assertEquals("TestFolder", createdRootNode.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, createdRootNode.getType());
      
      ConnectorNode newFolder = this.createFolder(createdRootNode, "ChildFolder");
      assertEquals("ChildFolder", newFolder.getLabel());
      assertEquals(ConnectorNodeType.FOLDER, newFolder.getType());
      
      ConnectorNode newModel = this.createModel(createdRootNode, "model1");
      assertEquals("model1", newModel.getLabel());
      assertEquals(ConnectorNodeType.FILE, newModel.getType());
      
      List<ConnectorNode> children = this.signavioConnector.getChildren(createdRootNode);
      
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
    ConnectorNode root = this.signavioConnector.getRoot();
    assertEquals("/", root.getId());
    assertEquals("/", root.getLabel());
    assertEquals(ConnectorNodeType.FOLDER, root.getType());
  }
  
  private ConnectorNode createPublicConnectorNode() {
    ConnectorNode publicNode = new ConnectorNode(SIGNAVIO_PUBLIC_FOLDER_ID, "Public");
    publicNode.setType(ConnectorNodeType.FOLDER);
    return publicNode;
  }

  private void login() {
    ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
    RegisterBuiltin.register(providerFactory);
    
    String signavioURL = (String) this.signavioConnector.getConfiguration().getProperties().get(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL) + "/p/";
    this.signavioClient = ProxyFactory.create(SignavioTestHelperClient.class, signavioURL);
    
    ConnectorConfiguration config = this.signavioConnector.getConfiguration();
    SignavioLoginForm loginForm = new SignavioLoginForm(config.getGlobalUser(), config.getGlobalPassword(), "true");
    Response resp = signavioClient.login(loginForm);
    
    if (resp.getStatus() != 200) {
      fail("Login to Signavio in SignavioConnectorTest failed!");
    }
    
    if (resp instanceof ClientResponse<?>) {
      ClientResponse<?> r = (ClientResponse<?>) resp;
      r.releaseConnection();
    }
    this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword());
    this.loggedIn = true;
  }
  
  private ConnectorNode createFolder(ConnectorNode parent, String folderName) throws Exception {
    if (!this.loggedIn) {
      this.login();
    }
    
    CreateSignavioFolderForm newFolderForm = new CreateSignavioFolderForm(folderName, "Folder to test SignavioConnector.", parent.getId());
    
    String result = signavioClient.createFolder(newFolderForm);
    JSONObject jsonObj = new JSONObject(result);
    
    ConnectorNode newNode = new ConnectorNode(); 
    String href = jsonObj.getString("href");
    href = href.replace("/directory", "");
    newNode.setId(href);
    
    JSONObject repObj = jsonObj.getJSONObject("rep");
    String name = repObj.getString("name");
    newNode.setLabel(name);
    
    newNode.setType(ConnectorNodeType.FOLDER);
    
    return newNode;
  }
  
  private ConnectorNode createModel(ConnectorNode parentFolder, String modelName) throws Exception {
    if (!this.loggedIn) {
      this.login();
    }
    
    CreateSignavioModelForm newModelForm = new CreateSignavioModelForm(modelName, parentFolder.getId());
    
    String result = signavioClient.createModel(newModelForm);
    JSONObject jsonObj = new JSONObject(result);
    
    ConnectorNode newNode = new ConnectorNode(); 
    String href = jsonObj.getString("href");
    href = href.replace("/model", "");
    newNode.setId(href);
    
    JSONObject repObj = jsonObj.getJSONObject("rep");
    String name = repObj.getString("name");
    newNode.setLabel(name);
    
    newNode.setType(ConnectorNodeType.FILE);
    
    return newNode;
  }
  
  private void deleteFolder(ConnectorNode folderToDelete) {
    if (!this.loggedIn) {
      this.login();
    }
    if (!folderToDelete.getType().equals(ConnectorNodeType.FOLDER)) {
      fail("Tried to delete folder but the assigned ConnectorNode was not a folder.");
    }
    this.signavioClient.deleteFolder(folderToDelete.getId());
  }
  
}
