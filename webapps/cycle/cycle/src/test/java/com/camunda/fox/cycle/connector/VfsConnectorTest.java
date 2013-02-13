package com.camunda.fox.cycle.connector;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.camunda.fox.cycle.connector.test.util.RepositoryUtil;
import com.camunda.fox.cycle.connector.vfs.VfsConnector;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;

/**
 *
 * @author nico.rehwaldt
 */
public class VfsConnectorTest extends AbstractConnectorTestBase {

  private static VfsConnector connector;

  private static final File VFS_DIRECTORY = new File("target/vfs-repository");

  @BeforeClass
  public static void beforeClass() throws Exception {
    ConnectorConfiguration config = new ConnectorConfiguration();
    
    String url = RepositoryUtil.createVFSRepository(VFS_DIRECTORY);
    
    config.getProperties().put(VfsConnector.BASE_PATH_KEY, url);
    
    // NOT a spring bean!
    connector = new VfsConnector();
    connector.setConfiguration(config);
    connector.init();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    connector.dispose();
    RepositoryUtil.clean(VFS_DIRECTORY);
  }

  @Override
  public Connector getConnector() {
    return connector;
  }
}
