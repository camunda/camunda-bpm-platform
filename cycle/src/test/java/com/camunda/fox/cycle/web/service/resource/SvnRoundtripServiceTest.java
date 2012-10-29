package com.camunda.fox.cycle.web.service.resource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tmatesoft.svn.core.SVNException;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.svn.SvnConnector;
import com.camunda.fox.cycle.connector.test.util.RepositoryUtil;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class SvnRoundtripServiceTest extends AbstractRoundtripServiceTest {

  private static Class<? extends Connector> CONNECTOR_CLS = SvnConnector.class;
  
  private static SvnConnector connector;
  
  private static final File SVN_DIRECTORY = new File("target/svn-repository");
  
  @Inject
  private ConnectorRegistry connectorRegistry;
  
  public static boolean initialized = false;
  
  @BeforeClass
  public static void beforeClass() throws Exception {

    String svnUrl = RepositoryUtil.createSVNRepository(SVN_DIRECTORY);

    ConnectorConfiguration config = new ConnectorConfiguration();

    config.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    config.getProperties().put(SvnConnector.CONFIG_KEY_REPOSITORY_PATH, svnUrl);

    // NOT a spring bean!
    connector = new SvnConnector();
    connector.setConfiguration(config);
    connector.init();
  }

  @Override
  protected void ensureConnectorInitialized() throws Exception {

    if (!initialized) {
      List<ConnectorConfiguration> configurations = getConnectorRegistry().getConnectorConfigurations(CONNECTOR_CLS);
      ConnectorConfiguration config = configurations.get(0);

      // put mock connector to registry
      connectorRegistry.getCache().put(config.getId(), connector);

      // fake some connector properties
      connector.getConfiguration().setId(config.getId());
      connector.getConfiguration().setConnectorClass(config.getConnectorClass());
      
      initialized = true;
    }

    setConnector(connector);
  }

  @AfterClass
  public static void afterClass() throws IOException {
    RepositoryUtil.clean(SVN_DIRECTORY);
  }
}
