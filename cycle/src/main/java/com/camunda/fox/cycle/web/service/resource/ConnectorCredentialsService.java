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

import com.camunda.fox.cycle.entity.ConnectorCredentials;
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

  /**
   * $resource specific methods
   */
  @GET
  public List<ConnectorCredentialsDTO> list() {
    return ConnectorCredentialsDTO.wrapAll(connectorCredentialsRepository.findAll());
  }

  @GET
  @Path("{id}")
  public ConnectorCredentialsDTO get(@PathParam("id") long id) {
    return ConnectorCredentialsDTO.wrap(connectorCredentialsRepository.findById(id));
  }

  @POST
  @Path("{id}")
  @Transactional
  public ConnectorCredentialsDTO update(ConnectorCredentialsDTO data) {
    long id = data.getId();

    ConnectorCredentials connectorCredentials = connectorCredentialsRepository.findById(id);
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
  @Path("fetchConnectorCredentials")
  public List<ConnectorCredentialsDTO> fetchConnectorCredentials(@QueryParam("userId") Long userId) {
    return ConnectorCredentialsDTO.wrapAll(connectorCredentialsRepository.fetchConnectorCredentialsByUser(userId));
  }
  
  /**
   * Updates the connector-credential with the given data
   *
   */
  private void update(ConnectorCredentials connectorCredentials, ConnectorCredentialsDTO data) {
    connectorCredentials.setUser(data.getUser());
    connectorCredentials.setPassword(data.getPassword());
  }
}
