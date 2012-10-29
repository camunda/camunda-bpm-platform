package com.camunda.fox.cycle.connector;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.camunda.fox.cycle.util.IoUtil;

/**
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractConnectorTestBase {

  public static final String TMP_DIR_NAME = "connector-test-tmp-dir";

  public static final ConnectorNode TMP_FOLDER = 
    new ConnectorNode("//" + TMP_DIR_NAME, TMP_DIR_NAME, ConnectorNodeType.FOLDER);

  /**
   * Returns the connector to be tested
   * @return 
   */
  public abstract Connector getConnector();

  @Test
  public void shouldCreateDirectory() throws Exception {
    
    Connector connector = getConnector();
    ConnectorNode tmpFolder = connector.createNode("//", TMP_DIR_NAME, ConnectorNodeType.FOLDER);
    
    assertThat(tmpFolder).isEqualTo(TMP_FOLDER);
    
    try {
      ContentInformation tmpFolderInfo = connector.getContentInformation(tmpFolder);
      fail("Obtaining connector info from folder should raise error");
    } catch (IllegalArgumentException e) {
      // anticipated
    }
  }

  @Test
  public void shouldImportDirectoryContents() throws Exception {
    // given
    Connector connector = getConnector();
    
    // not alphabetically ordered!
    String[] filesToImport = new String[] { "collaboration_impl.bpmn", "collaboration.bpmn" };
    
    // when
    for (String file: filesToImport) {
      InputStream is = getDiagramResourceAsStream(file);
      
      ConnectorNode fileNode = connector.createNode("//" + TMP_DIR_NAME, file, ConnectorNodeType.ANY_FILE);
      connector.updateContent(fileNode, is);
      
      IoUtil.closeSilently(is);
    }
    
    // then we should reach this point
  }

  @Test
  public void shouldNotThrowExceptionWhenObtainingContentInfoOfNonExistentFile() throws Exception {
    // give
    Connector connector = getConnector();
    
    // when
    ContentInformation info = connector.getContentInformation(new ConnectorNode("some-non-existent-file"));
    
    // then
    assertThat(info).isNotNull();
    assertThat(info.exists()).isFalse();
  }

  @Test
  public void shouldListDirectoryContentsAlphabeticallyOrdered() throws Exception {
    // given
    Connector connector = getConnector();
    
    // when
    List<ConnectorNode> nodes = connector.getChildren(TMP_FOLDER);
    
    // then
    assertThat(nodes).hasSize(2);
    
    ConnectorNode firstChildNode = nodes.get(0);
    
    // collaboration should appear first --> alphabetical order
    assertThat(firstChildNode.getId()).isEqualTo("//" + TMP_DIR_NAME + "/collaboration.bpmn");
    assertThat(firstChildNode.getType()).isEqualTo(ConnectorNodeType.BPMN_FILE);
  }

  @Test
  public void shouldGetSingleFileContents() throws Exception {
    // given 
    Connector connector = getConnector();
    
    InputStream originalInputStream = null;
    InputStream nodeInputStream = null;
    
    try {
      originalInputStream = getDiagramResourceAsStream("collaboration_impl.bpmn");
      byte[] originalBytes = IoUtil.readInputStream(originalInputStream, "class path is");

      // when
      nodeInputStream = connector.getContent(new ConnectorNode("//" + TMP_DIR_NAME + "/collaboration_impl.bpmn"));
      byte[] nodeBytes = IoUtil.readInputStream(nodeInputStream, "node input stream");

      // then
      assertThat(nodeBytes).isEqualTo(originalBytes);
    } finally {
      IoUtil.closeSilently(originalInputStream, nodeInputStream);
    }
  }

  @Test
  public void shouldUpdateSingleFileContents() throws Exception {
    // given 
    Connector connector = getConnector();
    
    InputStream originalInputStream = null;
    InputStream nodeInputStream = null;
    
    
    try {
      originalInputStream = getDiagramResourceAsStream("collaboration_impl.bpmn");
      byte[] inputBytes = IoUtil.readInputStream(originalInputStream, "class path is");

      ConnectorNode fileNode = new ConnectorNode("//" + TMP_DIR_NAME + "/collaboration.bpmn", "collaboration.bpmn");
      
      // when
      ContentInformation updatedContentInfo = connector.updateContent(
        fileNode, new ByteArrayInputStream(inputBytes));
      
      assertThat(updatedContentInfo).isNotNull();
      assertThat(updatedContentInfo.exists()).isTrue();
      
      // see if updated was set
      Date now = new Date();
      Assert.assertTrue(updatedContentInfo.getLastModified().getTime() <= now.getTime());
//      assertFalse(new Date().before(updatedContentInfo.getLastModified()));
//      assertFalse(now.getTime() <= updatedContentInfo.getLastModified().getTime());
      
      // see if file contents equal the new contents
      nodeInputStream = connector.getContent(fileNode);
      byte[] nodeBytes = IoUtil.readInputStream(nodeInputStream, "node input stream");

      // then
      assertThat(nodeBytes).isEqualTo(inputBytes);
    } finally {
      IoUtil.closeSilently(originalInputStream, nodeInputStream);
    }
  }

  private InputStream getDiagramResourceAsStream(String file) {
    return getClass().getResourceAsStream("/com/camunda/fox/cycle/roundtrip/" + file);
  }
}
