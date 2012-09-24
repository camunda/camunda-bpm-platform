package com.camunda.fox.cycle.connector.signavio;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;

import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.util.IoUtil;


public abstract class AbstractSignavioConnectorTest {
  
  @Inject
  private SignavioConnector signavioConnector;
  
  
  protected ConnectorNode createFolder(ConnectorNode parent, String folderName) throws Exception {
    if (this.signavioConnector.needsLogin()) {
      ConnectorConfiguration config = this.signavioConnector.getConfiguration();
      this.signavioConnector.init(config);
      this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword());
    }
    
    SignavioCreateFolderForm form = new SignavioCreateFolderForm(folderName, "", parent.getId());
    String result = this.signavioConnector.getSignavioClient().createFolder(form);
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
  
  protected ConnectorNode createEmptyModel(ConnectorNode parentFolder, String modelName) throws Exception {
    if (this.signavioConnector.needsLogin()) {
      ConnectorConfiguration config = this.signavioConnector.getConfiguration();
      this.signavioConnector.init(config);
      this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword());
    }
    
    SignavioCreateModelForm newModelForm = new SignavioCreateModelForm();
    
    newModelForm.setId(UUID.randomUUID().toString().replace("-", ""));
    newModelForm.setName(modelName);
    newModelForm.setComment("");
    newModelForm.setDescription("");
    newModelForm.setParent(parentFolder.getId());
    newModelForm.setJsonXml(IoUtil.readFileAsString("com/camunda/fox/cycle/connector/emptyProcessModelTemplate.json"));
    newModelForm.setSVG_XML("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>");

    String result = signavioConnector.getSignavioClient().createModel(newModelForm);
    JSONObject jsonObj = new JSONObject(result);
    
    ConnectorNode newNode = new ConnectorNode(); 
    String href = jsonObj.getString("href");
    href = href.replace("/model", "");
    newNode.setId(href);
    
    JSONObject repObj = jsonObj.getJSONObject("rep");
    String name = repObj.getString("name");
    newNode.setLabel(name);
    
    newNode.setType(ConnectorNodeType.BPMN_FILE);
    
    return newNode;
  }
  
  protected void deleteFolder(ConnectorNode folderToDelete) {
    if (this.signavioConnector.needsLogin()) {
      ConnectorConfiguration config = this.signavioConnector.getConfiguration();
      this.signavioConnector.init(config);
      this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword());
    }
    
    if (!folderToDelete.getType().isDirectory()) {
      fail("Tried to delete folder but the assigned ConnectorNode was not a folder.");
    }
    this.signavioConnector.getSignavioClient().deleteFolder(folderToDelete.getId());
  }

  protected void deleteModel(ConnectorNode modelToDelete) {
    if (this.signavioConnector.needsLogin()) {
      ConnectorConfiguration config = this.signavioConnector.getConfiguration();
      this.signavioConnector.init(config);
      this.signavioConnector.login(config.getGlobalUser(), config.getGlobalPassword());
    }
    
    if (!modelToDelete.getType().isFile()) {
      fail("Tried to delete folder but the assigned ConnectorNode was not a model.");
    }
    this.signavioConnector.getSignavioClient().deleteModel(modelToDelete.getId());
  }
  
  protected void importSignavioArchive(ConnectorNode folder, String signavioArchive) throws Exception {
    HttpClient httpClient = this.signavioConnector.getHttpClient4Executor().getHttpClient();
    String signavioURL = this.signavioConnector.getConfiguration().getProperties().get("signavioBaseUrl");
    if (signavioURL.endsWith("/")) {
      signavioURL = signavioURL + "p/";
    } else {
      signavioURL = signavioURL + "/" + "p/";
    }
    HttpPost post = new HttpPost(signavioURL + "zip-import");
    
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    
    entity.addPart("file", new FileBody(new File(signavioArchive)));
    
    String parentFolderId = folder.getId();
    entity.addPart("directory", new StringBody("/directory" + parentFolderId , Charset.forName("UTF-8")));
    entity.addPart("signavio-id", new StringBody(UUID.randomUUID().toString(), Charset.forName("UTF-8")));
    post.setEntity(entity);
    
    EntityUtils.toString(httpClient.execute(post).getEntity(), "UTF-8");
  }
  
  protected SignavioConnector getSignavioConnector() {
    return this.signavioConnector;
  }

}
