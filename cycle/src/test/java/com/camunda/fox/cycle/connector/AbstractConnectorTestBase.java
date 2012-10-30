package com.camunda.fox.cycle.connector;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

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
   *
   * @return
   */
  public abstract Connector getConnector();

  @Test
  public void shouldCreateDirectory() throws Exception {

    Connector connector = getConnector();
    ConnectorNode tmpFolder = connector.createNode("//", TMP_DIR_NAME, ConnectorNodeType.FOLDER);

    assertThat(tmpFolder).isEqualTo(TMP_FOLDER);

    try {
      connector.getContentInformation(tmpFolder);
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
    String[] filesToImport = new String[]{"test-rhs.bpmn", "test-lhs.bpmn"};

    // when
    for (String file : filesToImport) {
      importFile(connector, file, file);
    }

    // import another file with no extension
    importFile(connector, "test-rhs.bpmn", "test-rhs");

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
  public void shouldServePngImageForNoExtensionFiles() throws Exception {
    // give
    Connector connector = getConnector();

    // when
    ContentInformation info = connector.getContentInformation(new ConnectorNode("//" + TMP_DIR_NAME + "/test-rhs", ConnectorNodeType.PNG_FILE));

    // then
    assertThat(info).isNotNull();
    assertThat(info.exists()).isFalse();
  }

  @Test
  public void shouldServePngImage() throws Exception {
    // give
    Connector connector = getConnector();

    // when
    ContentInformation info = connector.getContentInformation(new ConnectorNode("//" + TMP_DIR_NAME + "/test-rhs.bpmn", ConnectorNodeType.PNG_FILE));

    // then
    assertThat(info).isNotNull();
    assertThat(info.exists()).isFalse();
  }

  @Test
  public void shouldServePngImage2() throws Exception {
    // give
    Connector connector = getConnector();

    // when
    ContentInformation info = connector.getContentInformation(new ConnectorNode("//" + TMP_DIR_NAME + "/test-rhs.png"));

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
    assertThat(nodes).hasSize(3);

    ConnectorNode firstChildNode = nodes.get(0);

    // collaboration should appear first --> alphabetical order
    assertThat(firstChildNode.getId()).isEqualTo("//" + TMP_DIR_NAME + "/test-lhs.bpmn");
    assertThat(firstChildNode.getType()).isEqualTo(ConnectorNodeType.BPMN_FILE);
  }

  @Test
  public void shouldGetSingleFileContents() throws Exception {
    // given 
    Connector connector = getConnector();

    InputStream originalInputStream = null;
    InputStream nodeInputStream = null;

    try {
      originalInputStream = getDiagramResourceAsStream("test-rhs.bpmn");
      byte[] originalBytes = IoUtil.readInputStream(originalInputStream, "class path is");

      // when
      nodeInputStream = connector.getContent(new ConnectorNode("//" + TMP_DIR_NAME + "/test-rhs.bpmn"));
      byte[] nodeBytes = IoUtil.readInputStream(nodeInputStream, "node input stream");

      // then
      assertThat(nodeBytes).isEqualTo(originalBytes);
    } finally {
      IoUtil.closeSilently(originalInputStream, nodeInputStream);
    }
  }

  @Test
  public void shouldUpdateSingleFileContentFromConnector() throws Exception {
    // given 
    Connector connector = getConnector();

    InputStream originalInputStream = null;
    InputStream nodeInputStream = null;

    ConnectorNode sourceFileNode = new ConnectorNode("//" + TMP_DIR_NAME + "/test-lhs.bpmn", "test-lhs.bpmn");

    // now, with seconds accuracy
    Date now = new Date(System.currentTimeMillis() * 1000 / 1000);

    try {
      originalInputStream = connector.getContent(sourceFileNode);
      byte[] inputBytes = IoUtil.readInputStream(originalInputStream, "connector is is");

      ConnectorNode destFileNode = connector.createNode("//" + TMP_DIR_NAME, "/test-lhs-copy.bpmn", ConnectorNodeType.ANY_FILE);


      // when
      ContentInformation updatedContentInfo = connector.updateContent(
        destFileNode, new ByteArrayInputStream(inputBytes));

      assertThat(updatedContentInfo).isNotNull();
      assertThat(updatedContentInfo.exists()).isTrue();

      assertCorrectLastModified(now, updatedContentInfo.getLastModified());

      // see if file contents equal the new contents
      nodeInputStream = connector.getContent(destFileNode);
      byte[] nodeBytes = IoUtil.readInputStream(nodeInputStream, "node input stream");

      // then
      assertThat(nodeBytes).isEqualTo(inputBytes);
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

    // now, with seconds accuracy
    Date now = new Date(System.currentTimeMillis() * 1000 / 1000);

    try {
      originalInputStream = getDiagramResourceAsStream("test-rhs.bpmn");
      byte[] inputBytes = IoUtil.readInputStream(originalInputStream, "class path is");

      ConnectorNode fileNode = new ConnectorNode("//" + TMP_DIR_NAME + "/test-lhs.bpmn", "test-lhs.bpmn");

      // when
      ContentInformation updatedContentInfo = connector.updateContent(
        fileNode, new ByteArrayInputStream(inputBytes));

      assertThat(updatedContentInfo).isNotNull();
      assertThat(updatedContentInfo.exists()).isTrue();

      assertCorrectLastModified(now, updatedContentInfo.getLastModified());

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
    return getClass().getResourceAsStream("/com/camunda/fox/cycle/roundtrip/repository/" + file);
  }

  private void importFile(Connector connector, String file, String connectorNodeName) throws Exception {
    InputStream is = getDiagramResourceAsStream(file);

    ConnectorNode fileNode = connector.createNode("//" + TMP_DIR_NAME, connectorNodeName, ConnectorNodeType.ANY_FILE);
    connector.updateContent(fileNode, is);

    IoUtil.closeSilently(is);
  }

  private void assertCorrectLastModified(Date comparisonDate, Date lastModified) {
    // see if updated was set
    assertThat(lastModified).isAfterOrEqualsTo(comparisonDate);
    assertThat(lastModified).isBeforeOrEqualsTo(new Date());
  }
}
