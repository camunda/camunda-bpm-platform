package org.camunda.bpm.cockpit.plugin.base.service;

import java.util.List;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessDefinitionFailureChainDto;
import org.camunda.bpm.cockpit.service.AbstractEngineAware;

/**
 *
 * @author nico.rehwaldt
 */
public class FailureService extends AbstractEngineAware {

  public FailureService(String engineName) {
    super(engineName);
  }

  public List<ProcessDefinitionFailureChainDto> getJobFailuresByProcessDefinition() {
    return getQueryService().executeQuery("cockpit.base.selectJobFailuresByProcessDefinition", new QueryParameters<ProcessDefinitionFailureChainDto>());
  }
}
