package org.camunda.bpm.cycle.connector;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.cycle.util.DateUtil;
import org.camunda.bpm.cycle.util.IoUtil;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractConnectorTestBase {

  public static final String TMP_DIR_NAME = "connector-test-tmp-dir";
  public static final ConnectorNode TMP_FOLDER =
    new ConnectorNode("//" + TMP_DIR_NAME, TMP_DIR_NAME, ConnectorNodeType.FOLDER);

  private ConnectorNode tmpFolder;

  /**
   * Returns the connector to be tested
   *
   * @return
   */
  public abstract Connector getConnector();

  @Before
  public void before() throws Exception {

    Connector connector = getConnector();
    if (connector.getNode(TMP_FOLDER.getId()) != null) {
      return;
    }

    String message = "create folder";
    tmpFolder = connector.createNode("//", TMP_DIR_NAME, ConnectorNodeType.FOLDER, message);
    if (connector.isSupportsCommitMessage()) {
      assertThat(tmpFolder.getMessage()).isEqualTo(message);
    }

    assertThat(tmpFolder).isEqualTo(TMP_FOLDER);

    ConnectorNode node = connector.getNode(tmpFolder.getId());
    if (connector.isSupportsCommitMessage()) {
      assertThat(node.getMessage()).isEqualTo(message);
    }

    try {
      connector.getContentInformation(tmpFolder);
      fail("Obtaining connector info from folder should raise error");
    } catch (IllegalArgumentException e) {
      // anticipated
    }

    // not alphabetically ordered!
    String[] filesToImport = new String[]{ "test-rhs.bpmn", "test-lhs.bpmn" };

    // when
    for (String file : filesToImport) {
      importFile(connector, file, file);
    }

    // import another file with no extension
    importFile(connector, "test-rhs.bpmn", "test-rhs");
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
    ConnectorNode destFileNode = null;

    // now, with seconds accuracy
    Date now = DateUtil.getNormalizedDate(System.currentTimeMillis());

    try {
      originalInputStream = connector.getContent(sourceFileNode);
      byte[] inputBytes = IoUtil.readInputStream(originalInputStream, "connector is is");

      destFileNode = connector.createNode("//" + TMP_DIR_NAME, "/test-lhs-copy.bpmn", ConnectorNodeType.ANY_FILE, null);

      // when
      ContentInformation updatedContentInfo = connector.updateContent(
        destFileNode, new ByteArrayInputStream(inputBytes), null);

      assertThat(updatedContentInfo).isNotNull();
      assertThat(updatedContentInfo.exists()).isTrue();

      assertCorrectLastModified(now, updatedContentInfo.getLastModified());

      // see if file contents equal the new contents
      nodeInputStream = connector.getContent(destFileNode);
      byte[] nodeBytes = IoUtil.readInputStream(nodeInputStream, "node input stream");

      // then
      assertThat(nodeBytes).isEqualTo(inputBytes);
    } finally {
      safeDeleteNode(destFileNode, connector);
      IoUtil.closeSilently(originalInputStream, nodeInputStream);
    }
  }

  @Test
  public void shouldSetCreateAndUpdateMessage() throws Exception {
    Connector connector = getConnector();
    ConnectorNode destFileNode = null;

    try {
      if (connector.isSupportsCommitMessage()) {
        // given

        String createMessage = "initial create";
        destFileNode = connector.createNode("//" + TMP_DIR_NAME, "/test-lhs-copy2.bpmn", ConnectorNodeType.ANY_FILE, createMessage);

        destFileNode = connector.getNode(destFileNode.getId());
        assertThat(destFileNode.getMessage()).isEqualTo(createMessage);

        // when
        String updateMessage = "updating node ...";
        connector.updateContent(destFileNode, new ByteArrayInputStream("Test".getBytes()), updateMessage);

        // now the message is equal to the update message
        destFileNode = connector.getNode(destFileNode.getId());
        assertThat(destFileNode.getMessage()).isEqualTo(updateMessage);

        connector.deleteNode(destFileNode, "cleanup");
      }
    } finally {
      safeDeleteNode(destFileNode, connector);
    }
  }

  @Test
  public void shouldSetDeleteMessage() throws Exception {
    Connector connector = getConnector();
    ConnectorNode destFileNode = null;

    try {
      if (connector.isSupportsCommitMessage()) {
        // given

        String createMessage = "initial create";
        destFileNode = connector.createNode("//" + TMP_DIR_NAME, "/test-lhs-copy3.bpmn", ConnectorNodeType.ANY_FILE, createMessage);

        destFileNode = connector.getNode(destFileNode.getId());
        assertThat(destFileNode.getMessage()).isEqualTo(createMessage);

        String deleteMessage = "deleting node ...";
        connector.deleteNode(destFileNode, deleteMessage);

        ConnectorNode parentNode = connector.getNode("//" + TMP_DIR_NAME);
        assertThat(parentNode.getMessage()).isEqualTo(deleteMessage);
      }
    } finally {
      safeDeleteNode(destFileNode, connector);
    }
  }

  @Test
  public void shouldUpdateSingleFileContents() throws Exception {
    // given
    Connector connector = getConnector();

    InputStream originalInputStream = null;
    InputStream nodeInputStream = null;

    // now, with seconds accuracy
    Date beforeUpdate = DateUtil.getNormalizedDate(System.currentTimeMillis());

    try {
      originalInputStream = getDiagramResourceAsStream("test-rhs.bpmn");
      byte[] inputBytes = IoUtil.readInputStream(originalInputStream, "class path is");

      ConnectorNode fileNode = new ConnectorNode("//" + TMP_DIR_NAME + "/test-lhs.bpmn", "test-lhs.bpmn");

      // when
      ContentInformation updatedContentInfo = connector.updateContent(
        fileNode, new ByteArrayInputStream(inputBytes), null);

      assertThat(updatedContentInfo).isNotNull();
      assertThat(updatedContentInfo.exists()).isTrue();

      assertCorrectLastModified(beforeUpdate, updatedContentInfo.getLastModified());

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
    return getClass().getResourceAsStream("/org/camunda/bpm/cycle/roundtrip/repository/" + file);
  }

  private void importFile(Connector connector, String file, String connectorNodeName) throws Exception {
    InputStream is = getDiagramResourceAsStream(file);

    ConnectorNode fileNode = connector.createNode("//" + TMP_DIR_NAME, connectorNodeName, ConnectorNodeType.ANY_FILE, null);
    connector.updateContent(fileNode, is, null);

    IoUtil.closeSilently(is);
  }

  private void assertCorrectLastModified(Date beforeUpdate, Date lastModified) {

    // see if updated was set
    // compare by time to mitigate problems with time zone comparison
    assertThat(lastModified.getTime()).isGreaterThanOrEqualTo(beforeUpdate.getTime());
    assertThat(lastModified.getTime()).isLessThanOrEqualTo(new Date().getTime());
  }

  private void safeDeleteNode(ConnectorNode destFileNode, Connector connector) {
    if (destFileNode != null) {
      try {
        connector.deleteNode(destFileNode, "cleanup");
      } catch (Exception e) {
        ;
      }
    }
  }
}
