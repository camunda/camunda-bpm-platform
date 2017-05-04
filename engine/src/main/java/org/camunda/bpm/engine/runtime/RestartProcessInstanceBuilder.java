package org.camunda.bpm.engine.runtime;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;

/**
 * 
 * @author Anna Pazola
 *
 */

public interface RestartProcessInstanceBuilder extends InstantiationBuilder<RestartProcessInstanceBuilder> {
  
  RestartProcessInstanceBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery query);
  
  RestartProcessInstanceBuilder processInstanceIds(String... processInstanceIds);
  
  RestartProcessInstanceBuilder processInstanceIds(List<String> ids);
  
  void execute();
  
  Batch executeAsync();

  
  

}
