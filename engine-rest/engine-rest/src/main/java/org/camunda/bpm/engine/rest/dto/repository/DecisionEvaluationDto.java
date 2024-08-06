package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;

import java.util.List;
import java.util.Map;

/**
 * GIT Issue: https://github.com/camunda/camunda-bpm-platform/issues/2749
 */
public class DecisionEvaluationDto {
    protected String decisionInstanceId;
    protected List<Map<String, VariableValueDto>> result;

    public String getDecisionInstanceId() {
        return decisionInstanceId;
    }

    public void setDecisionInstanceId(String decisionInstanceId) {
        this.decisionInstanceId = decisionInstanceId;
    }

    public List<Map<String, VariableValueDto>> getResult() {
        return result;
    }

    public void setResult(List<Map<String, VariableValueDto>> result) {
        this.result = result;
    }
}
