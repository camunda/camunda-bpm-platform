package org.camunda.bpm.client.task.impl.dto;


import org.camunda.bpm.client.impl.RequestDto;
import org.camunda.bpm.client.variable.impl.TypedValueField;

import java.util.Map;

public class SetVariablesRequestDto extends RequestDto {

    protected Map<String, TypedValueField> variables;

    public SetVariablesRequestDto(String workerId, Map<String, TypedValueField> variables) {
        super(workerId);
        this.variables = variables;
    }

    public Map<String, TypedValueField> getVariables() {
        return variables;
    }
}
