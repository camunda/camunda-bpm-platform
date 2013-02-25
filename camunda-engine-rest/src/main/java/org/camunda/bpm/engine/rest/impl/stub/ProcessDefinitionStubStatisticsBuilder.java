package org.camunda.bpm.engine.rest.impl.stub;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.StubProcessDefinitionDto;

public class ProcessDefinitionStubStatisticsBuilder {

  private ProcessDefinitionStatisticsResultDto currentStatisticsEntity;
  private List<StatisticsResultDto> collectedResults;
  private StubProcessDefinitionDto processDefinition;
  
  private ProcessDefinitionStubStatisticsBuilder() {
    currentStatisticsEntity = new ProcessDefinitionStatisticsResultDto();
    processDefinition = new StubProcessDefinitionDto();
    collectedResults = new ArrayList<StatisticsResultDto>();
  }
  
  public static ProcessDefinitionStubStatisticsBuilder addResult() {
    return new ProcessDefinitionStubStatisticsBuilder();
  }
  public ProcessDefinitionStubStatisticsBuilder nextResult() {
    currentStatisticsEntity.setDefinition(processDefinition);
    collectedResults.add(currentStatisticsEntity);
    currentStatisticsEntity = new ProcessDefinitionStatisticsResultDto();
    processDefinition = new StubProcessDefinitionDto();
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder id(String id) {
    currentStatisticsEntity.setId(id);
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder instances(int instances) {
    currentStatisticsEntity.setInstances(instances);
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder failedJobs(int failedJobs) {
    currentStatisticsEntity.setFailedJobs(failedJobs);
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder definitionId(String id) {
    processDefinition.setId(id);
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder definitionKey(String key) {
    processDefinition.setKey(key);
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder definitionName(String name) {
    processDefinition.setName(name);
    return this;
  }
  
  public ProcessDefinitionStubStatisticsBuilder definitionVersion(int version) {
    processDefinition.setVersion(version);
    return this;
  }
  
  public List<StatisticsResultDto> build() {
    currentStatisticsEntity.setDefinition(processDefinition);
    collectedResults.add(currentStatisticsEntity);
    return collectedResults;
  }
}
