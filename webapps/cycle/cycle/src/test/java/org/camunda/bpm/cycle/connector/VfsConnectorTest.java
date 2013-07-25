package org.camunda.bpm.cycle.connector;

import static org.camunda.bpm.cycle.connector.SvnConnectorTest.setupConnector;
import static org.camunda.bpm.cycle.connector.SvnConnectorTest.teardownConnector;
import java.io.File;

import org.camunda.bpm.cycle.connector.test.util.RepositoryUtil;
import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
 *
 * @author nico.rehwaldt
 */
public class VfsConnectorTest extends AbstractConnectorTestBase {

  private static VfsConnector connector;

  private static final File VFS_DIRECTORY = new File("target/vfs-repository");

  public static void setupConnector() throws Exception {
    ConnectorConfiguration config = new ConnectorConfiguration();

    String url = RepositoryUtil.createVFSRepository(VFS_DIRECTORY);

    config.getProperties().put(VfsConnector.BASE_PATH_KEY, url);

    // NOT a spring bean!
    connector = new VfsConnector();
    connector.setConfiguration(config);
    connector.init();
  }

  public static void teardownConnector() throws Exception {
    connector.dispose();
    RepositoryUtil.clean(VFS_DIRECTORY);
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    setupConnector();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    teardownConnector();
  }
  
  @Override
  public Connector getConnector() {
    return connector;
  }
}
