package org.camunda.bpm.client.task.impl.dto;

import org.camunda.bpm.client.impl.RequestDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.client.task.ExternalTask;

public class SendMessageRequestDto extends RequestDto {
    // no contain workerId

    protected String messageName;
    protected String businessKey;
    protected String tenantId;
    protected String processInstanceId;
    protected VariableMap correlationKeys;
    protected VariableMap processVariables;
    protected Boolean all;

    public SendMessageRequestDto(String workerId,ExternalTask externalTask, String messageName, VariableMap correlationKeys, VariableMap processVariables, Boolean all) {
        super(workerId);
        this.messageName = messageName;
        this.businessKey = externalTask.getBusinessKey();
        this.tenantId = externalTask.getTenantId();
        this.processInstanceId = externalTask.getProcessInstanceId();
        this.correlationKeys = correlationKeys;
        this.processVariables = processVariables;
        this.all = all;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public VariableMap getCorrelationKeys() {
        return correlationKeys;
    }

    public VariableMap getProcessVariables() {
        return processVariables;
    }

    public Boolean getAll() {
        return all;
    }
}