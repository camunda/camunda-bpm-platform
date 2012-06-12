package com.camunda.fox.tasklist;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;

@Named
@ViewScoped
public class ProcessStarter implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private RepositoryService repositoryService;

  @Inject
  private RuntimeService runtimeService;

  private List<ProcessDefinition> deployedProcesses;

  public List<ProcessDefinition> getDeployedProcesses() {
    if (deployedProcesses == null) {
      deployedProcesses = repositoryService.createProcessDefinitionQuery()
              .latestVersion()
              .orderByProcessDefinitionName().asc()
              .list();
    }
    return deployedProcesses;
  }

  public String startProcess(ProcessDefinition processDefinition) {
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    return "taskList.jsf";
  }

}
