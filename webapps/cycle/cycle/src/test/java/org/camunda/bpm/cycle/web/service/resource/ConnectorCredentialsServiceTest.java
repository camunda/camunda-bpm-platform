package org.camunda.bpm.cycle.web.service.resource;

import static org.fest.assertions.api.Assertions.*;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.cycle.connector.crypt.EncryptionService;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.entity.ConnectorCredentials;
import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.cycle.repository.ConnectorConfigurationRepository;
import org.camunda.bpm.cycle.repository.ConnectorCredentialsRepository;
import org.camunda.bpm.cycle.repository.UserRepository;
import org.camunda.bpm.cycle.web.dto.ConnectorCredentialsDTO;
import org.camunda.bpm.cycle.web.service.resource.ConnectorCredentialsService;
import org.junit.After;
import org.junit.Before;
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
public class ConnectorCredentialsServiceTest {

  @Inject
  private ConnectorCredentialsRepository credentialsRepository;
  
  @Inject
  private UserRepository userRepository;
  
  @Inject
  private ConnectorConfigurationRepository configurationRepository;
  
  @Inject
  private ConnectorCredentialsService connectorCredentialsService;
  
  @Inject
  private EncryptionService encryptionService;
  
  private User testUser;
  
  private ConnectorConfiguration config1;
  private ConnectorConfiguration config2;
  
  @Test
  public void shouldFailCreateWith400WhenNoUserId() throws Exception {
    
    // given
    ConnectorCredentialsDTO data = new ConnectorCredentialsDTO();
    data.setPassword("***");
    data.setUsername("klaus");
    
    try {
      // when
      connectorCredentialsService.create(data);
      fail("expected web application exception");
      
    } catch (WebApplicationException e) {
      assertThat(e.getResponse().getStatus()).isEqualTo(400);
    }
  }
  
  @Test
  public void shouldFailCreateWith400WhenNoConnectorId() throws Exception {
    
    // given
    ConnectorCredentialsDTO data = new ConnectorCredentialsDTO();
    data.setPassword("***");
    data.setUsername("klaus");
    data.setUserId(testUser.getId());
    
    try {
      // when
      connectorCredentialsService.create(data);
      fail("expected web application exception");
      
    } catch (WebApplicationException e) {
      assertThat(e.getResponse().getStatus()).isEqualTo(400);
    }
  }
  
  @Test
  public void shouldReturnDtoOnCreate() throws Exception {
    
    // given
    ConnectorCredentialsDTO data = new ConnectorCredentialsDTO();
    data.setPassword("***");
    data.setUsername("klaus");
    data.setUserId(testUser.getId());
    data.setConnectorId(config1.getId());
    
    // when
    ConnectorCredentialsDTO credentialsDTO = connectorCredentialsService.create(data);
    
    ConnectorCredentials credentialsFromDB = credentialsRepository.findById(credentialsDTO.getId());
    
    // then
    assertThat(credentialsDTO).isNotNull();
    assertThat(credentialsDTO.getId()).isNotNull();
    assertThat(encryptionService.decryptConnectorPassword(credentialsFromDB.getPassword())).isEqualTo("***");
    assertThat(credentialsFromDB).isNotNull();
  }
  
  @Test
  public void shouldGetCredentials() throws Exception {
    
    // given
    ConnectorCredentials credentials = new ConnectorCredentials("klaus", "***", config1, testUser);
    credentialsRepository.saveAndFlush(credentials);
    
    // when
    ConnectorCredentialsDTO credentialsDTO = connectorCredentialsService.get(credentials.getId());
    
    // then
    assertThat(credentialsDTO).isNotNull();
    assertThat(credentialsDTO.getId()).isEqualTo(credentials.getId());
    assertThat(credentialsDTO.getConnectorId()).isEqualTo(credentials.getConnectorConfiguration().getId());
    assertThat(credentialsDTO.getUserId()).isEqualTo(credentials.getUser().getId());
    
    // do not export the password !!!!11
    assertThat(credentialsDTO.getPassword()).isNull();
    
    assertThat(credentialsDTO.getUsername()).isEqualTo(credentials.getUsername());
  }
  
  @Test
  public void shouldUpdateCredentials() throws Exception {
    
    // given
    ConnectorCredentials credentials = new ConnectorCredentials("klaus", "***", config1, testUser);
    credentialsRepository.saveAndFlush(credentials);
    
    ConnectorCredentialsDTO updateDTO = new ConnectorCredentialsDTO();
    updateDTO.setPassword("***1");
    updateDTO.setUsername("klaus1");
    
    // that should be ignored by update
    updateDTO.setUserId(-1000);
    updateDTO.setConnectorId(-2000);
    
    updateDTO.setId(credentials.getId());
    
    // when
    ConnectorCredentialsDTO credentialsDTO = connectorCredentialsService.update(updateDTO);
    ConnectorCredentials credentialsFromDB = credentialsRepository.findById(credentials.getId());
    
    // then
    assertThat(credentialsDTO).isNotNull();
    assertThat(credentialsDTO.getId()).isEqualTo(credentials.getId());
    
    // do not export the password !!!!11
    assertThat(credentialsDTO.getPassword()).isNull();
    assertThat(credentialsDTO.getUsername()).isEqualTo(updateDTO.getUsername());
    
    // but password changed in database
    assertThat(encryptionService.decryptConnectorPassword(credentialsFromDB.getPassword())).isEqualTo(updateDTO.getPassword());
    
    // user and connector remain unchanged
    assertThat(credentialsDTO.getConnectorId()).isEqualTo(credentials.getConnectorConfiguration().getId());
    assertThat(credentialsDTO.getUserId()).isEqualTo(credentials.getUser().getId());
  }
  
  // Test bootstraping //////////////////////////////////////// 

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
    
    configurationRepository.saveAndFlush(config1);
    configurationRepository.saveAndFlush(config2);
  }
  
  @After
  public void after() throws Exception {
    credentialsRepository.deleteAll();
    configurationRepository.deleteAll();
    userRepository.deleteAll();
  }
}
