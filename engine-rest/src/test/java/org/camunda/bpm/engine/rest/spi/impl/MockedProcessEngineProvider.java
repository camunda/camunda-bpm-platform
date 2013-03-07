package org.camunda.bpm.engine.rest.spi.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class MockedProcessEngineProvider implements ProcessEngineProvider {

  private static ProcessEngine cachedDefaultProcessEngine;
  private static Map<String, ProcessEngine> cachedEngines = new HashMap<String, ProcessEngine>();
  
  private void mockServices(ProcessEngine engine) {
    RepositoryService repoService = mock(RepositoryService.class);
    IdentityService identityService = mock(IdentityService.class);
    TaskService taskService = mock(TaskService.class);
    RuntimeService runtimeService = mock(RuntimeService.class);
    FormService formService = mock(FormService.class);
    HistoryService historyService = mock(HistoryService.class);
    ManagementService managementService = mock(ManagementService.class);
    
    when(engine.getRepositoryService()).thenReturn(repoService);
    when(engine.getIdentityService()).thenReturn(identityService);
    when(engine.getTaskService()).thenReturn(taskService);
    when(engine.getRuntimeService()).thenReturn(runtimeService);
    when(engine.getFormService()).thenReturn(formService);
    when(engine.getHistoryService()).thenReturn(historyService);
    when(engine.getManagementService()).thenReturn(managementService);
  }

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    if (cachedDefaultProcessEngine == null) {
      cachedDefaultProcessEngine = mock(ProcessEngine.class);
      mockServices(cachedDefaultProcessEngine);
    }
    
    return cachedDefaultProcessEngine;
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    if (cachedEngines.get(name) == null) {
      ProcessEngine mock = mock(ProcessEngine.class);
      mockServices(mock);
      cachedEngines.put(name, mock);
    }
    
    return cachedEngines.get(name);
  }

}
