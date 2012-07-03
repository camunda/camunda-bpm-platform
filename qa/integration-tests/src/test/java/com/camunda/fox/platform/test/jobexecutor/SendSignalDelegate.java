package com.camunda.fox.platform.test.jobexecutor;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.BusinessProcess;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;

@Named
public class SendSignalDelegate implements JavaDelegate {

  @Inject
  private RuntimeService runtimeService;  

  @Inject
  private BusinessProcess businessProcess;   

  public void execute(DelegateExecution execution) throws Exception {
    businessProcess.setVariable("processName", "throwSignal-visited (was " + businessProcess.getVariable("processName")  + ")");

    String signalProcessInstanceId = (String) execution.getVariable("signalProcessInstanceId");      
    String executionId = runtimeService.createExecutionQuery().processInstanceId(signalProcessInstanceId).signalEventSubscriptionName("alert").singleResult().getId();      
    
    CommandContext commandContext = Context.getCommandContext();
    List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName = commandContext
            .getEventSubscriptionManager()
            .findSignalEventSubscriptionsByNameAndExecution("alert", executionId);

    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : findSignalEventSubscriptionsByEventName) {
        signalEventSubscriptionEntity.eventReceived(null, true);
    }       
  }
}
