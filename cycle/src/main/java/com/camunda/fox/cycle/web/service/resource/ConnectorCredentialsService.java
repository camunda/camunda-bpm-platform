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
import javax.ws.rs.core.MediaType;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.cycle.repository.ConnectorConfigurationRepository;
import com.camunda.fox.cycle.repository.ConnectorCredentialsRepository;
import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.cycle.web.dto.ConnectorCredentialsDTO;
import com.camunda.fox.cycle.web.service.AbstractRestService;

@Path("secured/resource/connector/credentials")
public class ConnectorCredentialsService extends AbstractRestService {
  
  @Inject
  private ConnectorCredentialsRepository connectorCredentialsRepository;
  
  @Inject
  private UserRepository userRepository;
  
  @Inject
  private ConnectorConfigurationRepository connectorConfigurationRepository;

   // $resource specific methods ///////////////////////////////////
  
  @GET
  public List<ConnectorCredentialsDTO> list(@QueryParam("userId") Long userId) {
    return fetchConnectorCredentialsByUserId(userId);
  }

  @GET
  @Path("{id}")
  public ConnectorCredentialsDTO get(@PathParam("id") long id) {
    return fetchConnectorCredentialsById(id);
  }

  @POST
  @Path("{id}")
  @Transactional
  public ConnectorCredentialsDTO update(ConnectorCredentialsDTO data) {
    long id = data.getId();

    ConnectorCredentials connectorCredentials = connectorCredentialsRepository.fetchConnectorCredentialsById(id);
    if (connectorCredentials == null) {
      throw notFound("ConnectorCredentials not found");
    }

    update(connectorCredentials, data);
    
    return ConnectorCredentialsDTO.wrap(connectorCredentials);
  }

  @POST
  public ConnectorCredentialsDTO create(ConnectorCredentialsDTO data) {
    ConnectorCredentials connectorCredentials = new ConnectorCredentials();
    update(connectorCredentials, data);
    
    ConnectorConfiguration config = connectorConfigurationRepository.findById(data.getConnectorId());
    if (config == null) {
      throw notFound("Connector configuration with id " + data.getConnectorId() + " not found.");
    }
    connectorCredentials.setConnectorConfiguration(config);
    
    User user = userRepository.findById(data.getUserId());
    if (user == null) {
      throw notFound("User with id " + data.getConnectorId() + " not found.");
    }
    connectorCredentials.setUser(user);
    
    return ConnectorCredentialsDTO.wrap(connectorCredentialsRepository.saveAndFlush(connectorCredentials));
  }
  
  @DELETE
  @Path("{id}")
  @Transactional
  public void delete(@PathParam("id") long id) {
    ConnectorCredentials connectorCredentials = connectorCredentialsRepository.findById(id);
    if (connectorCredentials == null) {
      throw notFound("ConnectorCredential not found");
    }
    connectorCredentialsRepository.delete(connectorCredentials);
  }
  
  // querying /////////////////////////////////////////////////////
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("fetchConnectorCredentialsByUserId")
  @Transactional
  public List<ConnectorCredentialsDTO> fetchConnectorCredentialsByUserId(@QueryParam("userId") Long userId) {
    return ConnectorCredentialsDTO.wrapAll(connectorCredentialsRepository.fetchConnectorCredentialsByUserId(userId));
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("fetchConnectorCredentialsById")
  @Transactional
  public ConnectorCredentialsDTO fetchConnectorCredentialsById(@QueryParam("id") Long id) {
    return ConnectorCredentialsDTO.wrap(connectorCredentialsRepository.fetchConnectorCredentialsById(id));
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("fetchConnectorCredentialsByUserIdAndConnectorId")
  @Transactional
  public ConnectorCredentialsDTO fetchConnectorCredentialsByUserIdAndConnectorId(@QueryParam("userId") Long userId, @QueryParam("connectorId") Long connectorId) {
    return ConnectorCredentialsDTO.wrap(connectorCredentialsRepository.fetchConnectorCredentialsByUserIdAndConnectorId(userId, connectorId));
  }
  
  /**
   * Updates the connector-credential with the given data
   *
   */
  private void update(ConnectorCredentials connectorCredentials, ConnectorCredentialsDTO data) {
    connectorCredentials.setUsername(data.getUsername());
    connectorCredentials.setPassword(data.getPassword());
  }
  
}
