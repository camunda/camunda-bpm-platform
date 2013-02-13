package com.camunda.fox.cycle.web.service.resource;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.ConnectorStatus;
import com.camunda.fox.cycle.connector.crypt.EncryptionService;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.cycle.repository.ConnectorConfigurationRepository;
import com.camunda.fox.cycle.repository.ConnectorCredentialsRepository;
import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.cycle.web.dto.ConnectorCredentialsDTO;
import com.camunda.fox.cycle.web.dto.ConnectorStatusDTO;
import com.camunda.fox.cycle.web.service.AbstractRestService;

/**
 * FIXME this class is untested!
 *
 */
@Path("secured/resource/connector/credentials")
public class ConnectorCredentialsService extends AbstractRestService {
  
  @Inject
  EncryptionService encryptionService;
  
  @Inject
  private ConnectorCredentialsRepository connectorCredentialsRepository;
  
  @Inject
  private UserRepository userRepository;
  
  @Inject
  private ConnectorConfigurationRepository connectorConfigurationRepository;
  
  @Inject
  private ConnectorRegistry connectorRegistry;

   // $resource specific methods ///////////////////////////////////
  
  @GET
  public List<ConnectorCredentialsDTO> list(@QueryParam("userId") Long userId) {
    return getConnectorCredentialsByUserId(userId);
  }

  @GET
  @Path("{id}")
  public ConnectorCredentialsDTO get(@PathParam("id") long id) {
    return ConnectorCredentialsDTO.wrap(getAndFetchConnectorCredentialsById(id));
  }

  @POST
  @Path("{id}")
  @Transactional
  public ConnectorCredentialsDTO update(ConnectorCredentialsDTO data) {
    long id = data.getId();
    ConnectorCredentials connectorCredentials = getById(id);

    update(connectorCredentials, data);
    
    return ConnectorCredentialsDTO.wrap(connectorCredentials);
  }

  @POST
  public ConnectorCredentialsDTO create(ConnectorCredentialsDTO data) {
    
    validate(data);
    
    ConnectorCredentials connectorCredentials = new ConnectorCredentials();
    update(connectorCredentials, data);
    
    ConnectorConfiguration config = getConnectorConfigurationById(data.getConnectorId());
    
    connectorCredentials.setConnectorConfiguration(config);
    
    User user = userRepository.findById(data.getUserId());
    if (user == null) {
      throw notFound("user with id " + data.getConnectorId() + " not found");
    }
    
    if (user.getPassword()!=null) {
      user.setPassword(encryptionService.encryptConnectorPassword(user.getPassword()));
    }
    connectorCredentials.setUser(user);
    
    return ConnectorCredentialsDTO.wrap(connectorCredentialsRepository.saveAndFlush(connectorCredentials));
  }
  
  @DELETE
  @Path("{id}")
  @Transactional
  public void delete(@PathParam("id") long id) {
    connectorCredentialsRepository.delete(id);
  }
  
  @POST
  @Path("test")
  public ConnectorStatusDTO test(ConnectorCredentialsDTO data) {
    ConnectorConfiguration config = getConnectorConfigurationById(data.getConnectorId());
    
    ConnectorStatus connectorStatus = connectorRegistry.testConnectorConfiguration(config, data.getUsername(), data.getPassword());
    return ConnectorStatusDTO.wrap(connectorStatus);
  }
  
  // querying /////////////////////////////////////////////////////
  
  protected List<ConnectorCredentialsDTO> getConnectorCredentialsByUserId(@QueryParam("userId") Long userId) {
    return ConnectorCredentialsDTO.wrapAll(connectorCredentialsRepository.findFetchConfigurationByUserId(userId));
  }
  
  protected ConnectorCredentials getAndFetchConnectorCredentialsById(@QueryParam("id") Long id) {
    ConnectorCredentials credentials = connectorCredentialsRepository.findFetchConfigurationById(id);
    if (credentials == null) {
      throw notFound("credentials not found");
    }
    return credentials;
  }

  protected ConnectorConfiguration getConnectorConfigurationById(long configurationId) {
    ConnectorConfiguration config = connectorConfigurationRepository.findById(configurationId);
    if (config == null) {
      throw notFound("configuration with id " + configurationId + " not found");
    }
    
    return config;
  }
  
  protected ConnectorCredentials getById(long id) throws WebApplicationException {
    ConnectorCredentials connectorCredentials = connectorCredentialsRepository.findById(id);
    if (connectorCredentials == null) {
      throw notFound("credentials not found");
    }
    return connectorCredentials;
  }

  /**
   * Updates the connector-credential with the given data
   *
   */
  private void update(ConnectorCredentials connectorCredentials, ConnectorCredentialsDTO data) {
    connectorCredentials.setUsername(data.getUsername());
    connectorCredentials.setPassword(encryptionService.encryptConnectorPassword(data.getPassword()));
  }

  // validation //////////////////////////////////////////////
  
  private void validate(ConnectorCredentialsDTO data) {
    if (data.getConnectorId() == -1) {
      throw badRequest("no connector id given");
    }
    if (data.getUserId() == -1) {
      throw badRequest("no user id given");
    }
  }
  
}
