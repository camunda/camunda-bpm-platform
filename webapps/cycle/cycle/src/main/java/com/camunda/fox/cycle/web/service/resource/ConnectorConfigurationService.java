package com.camunda.fox.cycle.web.service.resource;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.ConnectorStatus;
import com.camunda.fox.cycle.connector.crypt.EncryptionService;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.repository.ConnectorConfigurationRepository;
import com.camunda.fox.cycle.web.dto.ConnectorConfigurationDTO;
import com.camunda.fox.cycle.web.dto.ConnectorStatusDTO;
import com.camunda.fox.cycle.web.service.AbstractRestService;

@Path("secured/resource/connector/configuration")
public class ConnectorConfigurationService extends AbstractRestService {

  @Inject
  protected ConnectorRegistry connectorRegistry;
  
  @Inject
  protected EncryptionService encryptionService;

  @Inject
  protected ConnectorConfigurationRepository connectorConfigurationRepository;

  @GET
  @Path("defaults")
  public List<ConnectorConfigurationDTO> listDefaults() {
    return ConnectorConfigurationDTO.wrapAll(connectorRegistry.getConnectorDefinitions());
  }

  @GET
  public List<ConnectorConfigurationDTO> list() {
    return ConnectorConfigurationDTO.wrapAll(connectorConfigurationRepository.findAll());
  }

  @GET
  @Path("{id}")
  public ConnectorConfigurationDTO get(@PathParam("id") long id) {
    return ConnectorConfigurationDTO.wrap(findConfigurationById(id));
  }

  @POST
  @Path("{id}")
  @Transactional
  public ConnectorConfigurationDTO update(ConnectorConfigurationDTO data) {
    long id = data.getConnectorId();

    ConnectorConfiguration connectorConfiguration = findConfigurationById(id);

    update(connectorConfiguration, data);

    connectorConfigurationRepository.saveAndFlush(connectorConfiguration);

    // remove connector from cache; will be re-instantiated on demand
    connectorRegistry.getCache().remove(id);

    return ConnectorConfigurationDTO.wrap(connectorConfiguration);
  }

  @POST
  public ConnectorConfigurationDTO create(ConnectorConfigurationDTO data) {
    ConnectorConfiguration connectorConfiguration = createConfiguration(data);
    connectorConfiguration = connectorConfigurationRepository.saveAndFlush(connectorConfiguration);
    
    return ConnectorConfigurationDTO.wrap(connectorConfiguration);
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public void delete(@PathParam("id") long id) {
    ConnectorConfiguration connectorConfiguration = findConfigurationById(id);

    connectorConfigurationRepository.delete(connectorConfiguration);
    connectorRegistry.getCache().remove(id);
  }

  @POST
  @Path("test")
  public ConnectorStatusDTO test(ConnectorConfigurationDTO data) {
    ConnectorConfiguration connectorConfiguration = createConfiguration(data);
    
    ConnectorStatus connectorStatus = connectorRegistry.testConnectorConfiguration(connectorConfiguration);
    return ConnectorStatusDTO.wrap(connectorStatus);
  }

  private void update(ConnectorConfiguration config, ConnectorConfigurationDTO data) {
    if(data.getPassword() != null && !data.getPassword().isEmpty()) {
      config.setGlobalPassword(encryptionService.encryptConnectorPassword(data.getPassword()));
    }
    config.setGlobalUser(data.getUser());
    config.setLoginMode(data.getLoginMode());
    config.setProperties(data.getProperties());
    config.setName(data.getName());
  }

  private ConnectorConfiguration createConfiguration(ConnectorConfigurationDTO data) {
    ConnectorConfiguration config = null;
    if(data.getConnectorId() != null) {
      config = connectorConfigurationRepository.findById(data.getConnectorId());
    } else {
      config = new ConnectorConfiguration();
    }
    
    // do not make these things configurable on update
    config.setConnectorClass(data.getConnectorClass());
    config.setConnectorName(data.getConnectorName());

    // update the rest
    update(config, data);

    return config;
  }

  protected ConnectorConfiguration findConfigurationById(long id) {
    ConnectorConfiguration configuration = connectorConfigurationRepository.findById(id);
    
    if (configuration == null) {
      throw notFound("connector configuration not found");
    }
    
    return configuration;
  }
}
