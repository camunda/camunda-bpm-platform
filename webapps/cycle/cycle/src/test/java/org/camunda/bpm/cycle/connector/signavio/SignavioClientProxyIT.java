package org.camunda.bpm.cycle.connector.signavio;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.inject.Inject;

import junit.framework.Assert;

import org.camunda.bpm.cycle.connector.signavio.SignavioClient;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.connector.signavio.SignavioJson;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.http.ParseException;
import org.camunda.bpm.cycle.util.BpmnNamespaceContext;
import org.camunda.bpm.cycle.util.IoUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/test/signavio-connector-xml-config.xml" })
public class SignavioClientProxyIT {

  private static final String CREATE_FOLDER_NAME = "CreateFolder";

  @Inject
  private List<ConnectorConfiguration> connectorConfiguration;
  
  private SignavioClient signavioClient;
  private ConnectorConfiguration configuration;
  
  @Before
  public void setUp() throws Exception {
    configuration = connectorConfiguration.get(1);
    signavioClient = 
            new SignavioClient(configuration.getName(),
                               configuration.getProperties().get(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL),
                               configuration.getProperties().get(SignavioConnector.CONFIG_KEY_PROXY_URL),
                               configuration.getProperties().get(SignavioConnector.CONFIG_KEY_PROXY_USERNAME),
                               configuration.getProperties().get(SignavioConnector.CONFIG_KEY_PROXY_PASSWORD),
                               "");
    assertTrue("Failed to login.", signavioClient.login(configuration.getGlobalUser(), configuration.getGlobalPassword()));
  }

  @After
  public void tearDown() throws Exception {
    configuration = null;
    signavioClient.dispose();
    signavioClient = null;
  }

  @Test
  public void testGetChildren() {
    String children = signavioClient.getChildren("");
    assertThat(children).contains("private");
  }

  @Test
  public void testGetFolderInfo() throws JSONException {
    String folderId = createFolder();
    
    try {
      String info = signavioClient.getInfo(SignavioClient.DIRECTORY_URL_SUFFIX, folderId);
      assertThat(info).contains(CREATE_FOLDER_NAME);
    } finally {
      deleteFolder(folderId);
    }
  }

  @Ignore
  @Test
  public void testGetModelRepresentations() throws JSONException {
    // SVG, PNG, XML, JSON, INFO
    fail("Not implemented yet!");
    
    String folderId = createFolder();
    
    try {
      
    } finally {
      deleteFolder(folderId);
    }
  }

  @Test
  public void testUpdateModel() throws JSONException, ParseException, IOException {
    String folderId = createFolder();
    
    try {
      // create empty model
      String label = "CreateModel-" + new Date();
      String createdModel = signavioClient.createModel(folderId, label, "create empty model");
      assertThat(createdModel).contains(label);
      String modelId = SignavioJson.extractModelId(new JSONObject(createdModel));
      Assert.assertEquals("create empty model", SignavioJson.extractModelComment(new JSONObject(createdModel)));
      
      // import new model content
      String modelName = "HEMERA-2219";
      String newContent = new Scanner(getClass().getResourceAsStream("/models/" + modelName + "-import.bpmn"), "UTF-8").useDelimiter("\\A").next();
      signavioClient.importBpmnXml(folderId, newContent, modelName);
      
      // retrieve imported model id
      String children = signavioClient.getChildren(folderId);
      String importedModelId = SignavioJson.extractIdForMatchingModelName(children, modelName);
  
      String importedModelJson = signavioClient.getModelAsJson(importedModelId);
      String importedModelSvg = signavioClient.getModelAsSVG(importedModelId);
      
      // update model
      String comment = "updating model...";
      String updatedModel = signavioClient.updateModel(modelId, label, importedModelJson, importedModelSvg, folderId, comment);
      assertThat(updatedModel).contains(comment);
      
      // compare model contents
      InputStream newXmlContentStream = signavioClient.getXmlContent(modelId);
      byte[] newXmlContent = IoUtil.readInputStream(newXmlContentStream, "newXmlContent");
      IoUtil.closeSilently(newXmlContentStream);
      
      InputStream importedXmlContentStream = signavioClient.getXmlContent(modelId);
      byte[] importedXmlContent = IoUtil.readInputStream(importedXmlContentStream, "newXmlContent");
      IoUtil.closeSilently(importedXmlContentStream);
      
      DetailedDiff details = compareSignavioBpmn20Xml(new String(importedXmlContent, Charset.forName("UTF-8")),
                               new String(newXmlContent, Charset.forName("UTF-8")));
      assertTrue("Comparison:" + "\n" + details.toString().replaceAll("\\[not identical\\] [^\n]+\n", "").replaceAll("\n\n+", "\n"), details.similar());
    } finally {
      deleteFolder(folderId);
    }
  }

  @Test
  public void testCreateAndDeleteModel() throws JSONException {
    String folderId = createFolder();
    
    try {
      // create
      String label = "CreateModel-" + new Date();
      String createdModel = signavioClient.createModel(folderId, label, null);
      assertThat(createdModel).contains(label);
      
      // delete
      String deleteResponse = signavioClient.delete(SignavioClient.MODEL_URL_SUFFIX, 
                                                    SignavioJson.extractModelId(new JSONObject(createdModel)));
      assertThat(deleteResponse).contains("\"success\":true");
    } finally {
      deleteFolder(folderId);
    }
  }

  @Test
  public void testCreateAndDeleteFolder() throws JSONException {
    String folderId = createFolder();
    deleteFolder(folderId);
  }

  @Test
  public void testImportBpmnXml() throws JSONException, ParseException, IOException {
    String folderId = createFolder();
    
    try {
      String modelContent = new Scanner(getClass().getResourceAsStream("/models/HEMERA-2219-import.bpmn"), "UTF-8").useDelimiter("\\A").next();
      String response = signavioClient.importBpmnXml(folderId, modelContent, "HEMERA-2219");
    } finally {
      deleteFolder(folderId);
    }
  }

  @Test
  public void testImportSignavioArchive() throws ParseException, IOException, JSONException {
    String folderId = createFolder();
    
    try {
      String response = signavioClient.importSignavioArchive(folderId, "src/test/resources/models/HEMERA-2219.sgx");
      assertThat(response).isEqualToIgnoringCase("{\"warnings\":[]}");
    } finally {
      deleteFolder(folderId);
    }
  }

  @Test(expected=CycleException.class)
  public void testDispose() {
    signavioClient.dispose();
    // should throw NPE
    signavioClient.getChildren("");
  }

  private String createFolder() throws JSONException {
    // create folder
    String name = CREATE_FOLDER_NAME + "-" + new Date();
    String parentId = SignavioJson.extractPrivateFolderId(signavioClient.getChildren(""));
    String newFolder = signavioClient.createFolder(name, parentId);
    assertThat(newFolder).contains(name);
    assertThat(newFolder).contains(parentId);
    
    // extract id
    String id = SignavioJson.extractDirectoryId(new JSONObject(newFolder));
    assertThat(id).isNotEmpty();
    
    return id;
  }
  
  private void deleteFolder(String folderId) {
    String deleteResponse = signavioClient.delete(SignavioClient.DIRECTORY_URL_SUFFIX, folderId);
    assertThat(deleteResponse).contains("\"success\":true");
  }
  
  /**
   * Compares two BPMN 2.0 XML files exported by Signavio using XMLUnit.
   * Stolen from {@link SignavioConnectorIT}
   */
  private DetailedDiff compareSignavioBpmn20Xml(String expectedRawBpmn20Xml, String actualRawBpmn20Xml) {
    try {
      XMLUnit.setIgnoreWhitespace(true);
      XMLUnit.setIgnoreAttributeOrder(true);
      XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(new BpmnNamespaceContext().getNamespaces()));
      
      Diff diff = XMLUnit.compareXML(expectedRawBpmn20Xml, actualRawBpmn20Xml);
      DetailedDiff details = new DetailedDiff(diff);
      details.overrideDifferenceListener(new SignavioBpmn20XmlDifferenceListener());
      details.overrideElementQualifier(new ElementNameAndAttributeQualifier() {
        @Override
        public boolean qualifyForComparison(Element control, Element test) {
          if (test.getLocalName().equals("outgoing")) {
            return super.qualifyForComparison(control, test) && control.getTextContent().equals(test.getTextContent());  
          }
          return super.qualifyForComparison(control, test);
        }
      });
      return details;
    } catch (SAXException e) {
      throw new RuntimeException("Exception during XML comparison.", e);
    } catch (IOException e) {
      throw new RuntimeException("Exception during XML comparison.", e);
    }
  }
}
