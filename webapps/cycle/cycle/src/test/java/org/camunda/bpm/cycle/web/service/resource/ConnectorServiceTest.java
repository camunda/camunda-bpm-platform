package org.camunda.bpm.cycle.web.service.resource;

import java.io.File;
import static org.fest.assertions.api.Assertions.*;

import javax.inject.Inject;

import org.camunda.bpm.cycle.connector.ConnectorLoginMode;
import org.camunda.bpm.cycle.connector.ConnectorRegistry;
import org.camunda.bpm.cycle.connector.ConnectorStatus;
import org.camunda.bpm.cycle.connector.crypt.EncryptionService;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.connector.test.util.ConnectorConfigurationProvider;
import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.cycle.repository.ConnectorConfigurationRepository;
import org.camunda.bpm.cycle.repository.UserRepository;
import org.camunda.bpm.cycle.security.IdentityHolder;
import org.camunda.bpm.cycle.web.dto.ConnectorConfigurationDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorStatusDTO;
import org.camunda.bpm.cycle.web.service.resource.ConnectorConfigurationService;
import org.camunda.bpm.security.UserIdentity;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class ConnectorServiceTest {
  
  @Inject
  private ConnectorConfigurationProvider configurationProvider;
  
  @Inject
  private ConnectorConfigurationRepository configurationRepository;
  
  @Inject
  private ConnectorConfigurationService connectorConfigurationService;
  
  @Inject
  private ConnectorRegistry connectorRegistry;
  
  @Inject
  private UserRepository userRepository;
  
  private User testUser;
  
  @Test
  @Ignore
  // FIXME: TEST
  public void shouldCreate() throws Exception {
    fail("not tested");
  }
  
  @Test
  @Ignore
  // FIXME: TEST
  public void shouldGet() throws Exception {
    fail("not tested");
  }
  
  @Test
  @Ignore
  // FIXME: TEST
  public void shouldUpdate() throws Exception {
    fail("not tested");
  }
  
  @Test
  public void shouldTest() throws Exception {
    
    ConnectorConfiguration config = connectorRegistry.getConnectorDefinition(VfsConnector.class);
    
    // given
    ConnectorConfigurationDTO data = new ConnectorConfigurationDTO(config);
    data.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    data.setName("Test Connector");
    data.setConnectorId(config.getId());
    
    data.getProperties().put(VfsConnector.BASE_PATH_KEY, new File("target").getAbsolutePath());
    
    // when
    ConnectorStatusDTO testResult = connectorConfigurationService.test(data);
    
    // then
    assertThat(testResult.getState()).isEqualTo(ConnectorStatus.State.OK);
    assertThat(testResult.getExceptionMessage()).isNull();
    assertThat(testResult.getMessage()).isNull();
  }
  
  @Test
  public void shouldFailTestWithoutConfiguredUserCredentials() throws Exception {
    ConnectorConfiguration config = connectorRegistry.getConnectorDefinition(SignavioConnector.class);
    
    // given
    ConnectorConfigurationDTO data = new ConnectorConfigurationDTO(config);
    data.setLoginMode(ConnectorLoginMode.USER);
    data.setName("Test Connector");
    data.setConnectorId(config.getId());
    
    // when
    ConnectorStatusDTO testResult = connectorConfigurationService.test(data);
    
    // then
    assertThat(testResult.getState()).isEqualTo(ConnectorStatus.State.IN_ERROR);
    assertThat(testResult.getExceptionMessage()).contains("CycleMissingCredentialsException");
  }
  
  @Test
  public void shouldFailTestWithoutConfiguredUserCredentialsAndNoUserIdentity() throws Exception {
    ConnectorConfiguration config = connectorRegistry.getConnectorDefinition(SignavioConnector.class);
    
    // given
    ConnectorConfigurationDTO data = new ConnectorConfigurationDTO(config);
    data.setLoginMode(ConnectorLoginMode.USER);
    data.setName("Test Connector");
    data.setConnectorId(config.getId());
    
    // set current user identity
    IdentityHolder.setIdentity(new UserIdentity(testUser));
    
    // when
    ConnectorStatusDTO testResult = connectorConfigurationService.test(data);
    
    // then
    assertThat(testResult.getState()).isEqualTo(ConnectorStatus.State.IN_ERROR);
    assertThat(testResult.getExceptionMessage()).contains("CycleMissingCredentialsException");
  }
  
  // Test bootstraping //////////////////////////////////////// 

  @Before
  @Transactional
  public void before() throws Exception {
    testUser = new User("Achim", true);
    userRepository.saveAndFlush(testUser);
  }
  
  @After
  public void after() throws Exception {
    
    IdentityHolder.setIdentity(null);
    
    configurationRepository.deleteAll();
    userRepository.deleteAll();
  }
}
