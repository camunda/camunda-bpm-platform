package com.camunda.fox.cycle.repository;

import static org.fest.assertions.api.Assertions.*;

import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/context.xml", "classpath:/spring/test-*.xml"}
)
public class ConnectorCredentialsRepositoryTest {
  
  @Inject
  private UserRepository userRepository;
  
  @Inject
  private ConnectorCredentialsRepository credentialsRepository;
  
  @Inject
  private ConnectorConfigurationRepository connectorRepository;

  private User testUser;
  
  private ConnectorConfiguration config1;
  private ConnectorConfiguration config2;
  
  @Before
  @Transactional
  public void before() throws Exception {
    testUser = new User("Achim", true);
    userRepository.saveAndFlush(testUser);
    
    config1 = new ConnectorConfiguration();
    config1.setName("aaa");
    config1.setConnectorClass("my.bar");
    
    config2 = new ConnectorConfiguration();
    config2.setName("ddd");
    config2.setConnectorClass("my.foo");
    
    connectorRepository.saveAndFlush(config1);
    connectorRepository.saveAndFlush(config2);
  }
  
  @After
  public void after() throws Exception {
    credentialsRepository.deleteAll();
    connectorRepository.deleteAll();
    userRepository.deleteAll();
  }
  
  @Test
  public void shouldFindFetchConfigurationByUserId() {
    
    // given
    ConnectorCredentials credentials = new ConnectorCredentials("Asd", "sdf", config1, testUser);
    credentialsRepository.saveAndFlush(credentials);
    
    // when
    List<ConnectorCredentials> credentialList = credentialsRepository.findFetchConfigurationByUserId(testUser.getId());
    
    // then
    assertThat(credentialList).hasSize(1);
    assertThat(credentialList.get(0).getConnectorConfiguration()).isNotNull();
  }
  
  @Test
  public void shouldFindFetchConfigurationById() {
    
    // given
    ConnectorCredentials credentials = new ConnectorCredentials("Asd", "sdf", config1, testUser);
    credentialsRepository.saveAndFlush(credentials);
    
    // when
    ConnectorCredentials credentialsFromDB = credentialsRepository.findFetchConfigurationById(credentials.getId());
    
    // then
    assertThat(credentialsFromDB).isNotNull();
    assertThat(credentialsFromDB.getConnectorConfiguration()).isNotNull();
  }
  
  @Test
  public void shouldFindFetchAllByUsernameAndConnectorId() {
    
    // given
    ConnectorCredentials credentials1 = new ConnectorCredentials("Asd", "sdf", config1, testUser);
    ConnectorCredentials credentials2 = new ConnectorCredentials("Asd", "sdf", config2, testUser);
    credentialsRepository.saveAndFlush(credentials1);
    credentialsRepository.saveAndFlush(credentials2);
    
    // when
    ConnectorCredentials credentialsFromDB = credentialsRepository.findFetchAllByUsernameAndConnectorId(testUser.getName(), config1.getId());
    
    // then
    assertThat(credentialsFromDB).isNotNull();
    assertThat(credentialsFromDB.getConnectorConfiguration()).isNotNull();
    assertThat(credentialsFromDB.getUser()).isNotNull();
  }
  
  @Test
  public void shouldRemoveCredentialsWhenRemovingConnector() {
    
    // given
    ConnectorCredentials credentials1 = new ConnectorCredentials("Asd", "sdf", config1, testUser);
    credentialsRepository.saveAndFlush(credentials1);
    
    // when
    connectorRepository.delete(config1);
    
    ConnectorConfiguration deletedConfiguration = connectorRepository.findById(config1.getId());
    ConnectorCredentials deletedCredentials = credentialsRepository.findById(credentials1.getId());
    
    // then
    assertThat(deletedConfiguration).isNull();
    assertThat(deletedCredentials).isNull();
  }
  
  @Test
  public void findFetchAllByUserIdAndConnectorId() {
    
    // given
    ConnectorCredentials credentials1 = new ConnectorCredentials("Asd", "sdf", config1, testUser);
    ConnectorCredentials credentials2 = new ConnectorCredentials("Asd", "sdf", config2, testUser);
    credentialsRepository.saveAndFlush(credentials1);
    credentialsRepository.saveAndFlush(credentials2);
    
    // when
    ConnectorCredentials credentialsFromDB = credentialsRepository.findFetchAllByUserIdAndConnectorId(testUser.getId(), config2.getId());
    
    // then
    assertThat(credentialsFromDB).isNotNull();
    assertThat(credentialsFromDB.getConnectorConfiguration()).isNotNull();
    assertThat(credentialsFromDB.getUser()).isNotNull();
  }
}
