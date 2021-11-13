package org.camunda.bpm.client.listener;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DefaultClientInteractionListener implements ClientInteractionListener {

    @Override
    public void onFetchAndLock(List<TopicRequestDto> topics) {

    }

    @Override
    public void fetchAndLockDone(List<TopicRequestDto> topics, List<ExternalTask> externalTasks) {

    }

    @Override
    public void fetchAndLockFail(List<TopicRequestDto> topics, EngineClientException e) {

    }

    @Override
    public void onUnlock(String taskId) {

    }

    @Override
    public void unlockDone(String taskId) {

    }

    @Override
    public void unlockFail(String taskId) {

    }

    @Override
    public void onSetVariable(String processInstanceId, Map<String, Object> variables) {

    }

    @Override
    public void setVariableDone(String processInstanceId, Map<String, Object> variables) {

    }

    @Override
    public void setVariableFail(String processInstanceId, Map<String, Object> variables) {

    }

    @Override
    public void onComplete(String taskId, Map<String, Object> variables, Map<String, Object> localVariables) {

    }

    @Override
    public void completeDone(String taskId, Map<String, Object> variables, Map<String, Object> localVariables) {

    }

    @Override
    public void completeFail(String taskId, Map<String, Object> variables, Map<String, Object> localVariables) {

    }

    @Override
    public void onFailure(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout) {

    }

    @Override
    public void failureDone(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout) {

    }

    @Override
    public void failureFail(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout) {

    }

    @Override
    public void onBpmnError(String taskId, String errorCode, String errorMessage, Map<String, Object> variables) {

    }

    @Override
    public void bpmnErrorDone(String taskId, String errorCode, String errorMessage, Map<String, Object> variables) {

    }

    @Override
    public void bpmnErrorFail(String taskId, String errorCode, String errorMessage, Map<String, Object> variables) {

    }

    @Override
    public void onExtendLock(String taskId, long newDuration) {

    }

    @Override
    public void extendLockDone(String taskId, long newDuration) {

    }

    @Override
    public void extendLockFail(String taskId, long newDuration) {

    }
}