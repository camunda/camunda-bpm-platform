package com.camunda.fox.cycle.web.service.resource;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.repository.ConnectorConfigurationRepository;
import com.camunda.fox.cycle.web.dto.ConnectorConfigurationDTO;

@Path("secured/resource/connector/configuration")
public class ConnectorConfigurationService {
  
  @Inject
  protected ConnectorRegistry connectorRegistry;
  @Inject
  protected ConnectorConfigurationRepository connectorConfigurationRepository;
  
  @GET
  public List<ConnectorConfigurationDTO> list() {
    return ConnectorConfigurationDTO.wrapAll(connectorRegistry.getConnectorConfigurations());
  }
  
  @GET
  @Path("{id}")
  public ConnectorConfigurationDTO get(@PathParam("id") long id) {
    return ConnectorConfigurationDTO.wrap(connectorConfigurationRepository.findById(id));
  }

  @POST
  @Path("{id}")
  @Transactional
  public ConnectorConfigurationDTO update(ConnectorConfigurationDTO data) {
    long id = data.getConnectorId();
    
    ConnectorConfiguration connectorConfiguration = connectorConfigurationRepository.findById(id);
    if (connectorConfiguration == null) {
      throw new IllegalArgumentException("Not found");
    }
    
    update(connectorConfiguration, data);

    connectorConfigurationRepository.saveAndFlush(connectorConfiguration);
    connectorRegistry.updateConnectorInCache(id);
    return ConnectorConfigurationDTO.wrap(connectorConfiguration);
  }

  @POST
  public ConnectorConfigurationDTO create(ConnectorConfigurationDTO data) {
    ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration();
    update(connectorConfiguration, data);
    connectorConfiguration = connectorConfigurationRepository.saveAndFlush(connectorConfiguration);
    connectorRegistry.addConnectorToCache(connectorConfiguration.getId());
    return ConnectorConfigurationDTO.wrap(connectorConfiguration);
  }
  
  @POST
  @Path("{id}/delete")
  @Transactional
  public void delete(@PathParam("id") long id) {
    ConnectorConfiguration connectorConfiguration = connectorConfigurationRepository.findById(id);
    if (connectorConfiguration == null) {
      throw new IllegalArgumentException("Not found");
    }
    connectorConfigurationRepository.delete(connectorConfiguration);
    connectorRegistry.deleteConnectorFromCache(id);
  }
  
  private void update(ConnectorConfiguration connectorConfiguration, ConnectorConfigurationDTO data) {
    connectorConfiguration.setGlobalPassword(data.getPassword());
    connectorConfiguration.setGlobalPassword(data.getPassword());
    connectorConfiguration.setGlobalUser(data.getUser());
    connectorConfiguration.setLoginMode(ConnectorLoginMode.valueOf(data.getLoginMode()));
    connectorConfiguration.setProperties(data.getProperties());
    connectorConfiguration.setLabel(data.getName());
    connectorConfiguration.setConnectorClass(data.getConnectorClass());
  }
 
}
