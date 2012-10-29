package com.camunda.fox.cycle.web.service.resource;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.test.util.RepositoryUtil;
import com.camunda.fox.cycle.connector.vfs.VfsConnector;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class VfsRoundtripServiceTest extends AbstractRoundtripServiceTest {

  private static VfsConnector connector;
  
  private static final File VFS_DIRECTORY = new File("target/vfs-repository");
  
  private static Class<? extends Connector> CONNECTOR_CLS = VfsConnector.class;

  @Inject
  private ConnectorRegistry connectorRegistry;

  @Override
  protected void ensureConnectorInitialized() throws Exception {
    List<ConnectorConfiguration> configurations = getConnectorRegistry().getConnectorConfigurations(CONNECTOR_CLS);
    ConnectorConfiguration config = configurations.get(0);

    // put mock connector to registry
    connectorRegistry.getCache().put(config.getId(), connector);

    // fake some connector properties
    connector.getConfiguration().setId(config.getId());
    connector.getConfiguration().setConnectorClass(config.getConnectorClass());

    setConnector(connector);
  }

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
    RepositoryUtil.clean(VFS_DIRECTORY);
  }
}
