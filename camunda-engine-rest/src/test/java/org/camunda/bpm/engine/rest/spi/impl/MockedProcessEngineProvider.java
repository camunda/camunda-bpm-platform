package org.camunda.bpm.engine.rest.spi.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class MockedProcessEngineProvider implements ProcessEngineProvider {

  private static ProcessEngine cachedProcessEngine;
  
  @Override
  public synchronized ProcessEngine getProcessEngine() {
    if (cachedProcessEngine == null) {
      cachedProcessEngine = mock(ProcessEngine.class);
      mockServices(cachedProcessEngine);
    }
    
    return cachedProcessEngine;
  }
  
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

}
