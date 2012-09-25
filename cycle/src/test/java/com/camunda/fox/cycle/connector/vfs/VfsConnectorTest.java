package com.camunda.fox.cycle.connector.vfs;

import java.io.File;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.AbstractConnectorTestBase;
import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class VfsConnectorTest extends AbstractConnectorTestBase {

  private static VfsConnector connector;
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    ConnectorConfiguration config = new ConnectorConfiguration();
    
    URI targetFolderUri = new File("target").toURI();
    
    config.getProperties().put(VfsConnector.BASE_PATH_KEY, targetFolderUri.toString());
    config.setGlobalUser("user");
    config.setGlobalPassword("password");
    
    // NOT a spring bean!
    connector = new VfsConnector();
    connector.setConfiguration(config);
    connector.init();
  }
  
  @AfterClass
  public static void afterClass() throws Exception {
    FileUtils.deleteDirectory(new File("target/" + TMP_DIR_NAME));
  }
  
  @Override
  public Connector getConnector() {
    return connector;
  }
}
