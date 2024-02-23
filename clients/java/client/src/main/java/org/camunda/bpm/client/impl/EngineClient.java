import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.client.task.OrderingConfig;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.task.impl.dto.BpmnErrorRequestDto;
import org.camunda.bpm.client.task.impl.dto.CompleteRequestDto;
import org.camunda.bpm.client.task.impl.dto.ExtendLockRequestDto;
import org.camunda.bpm.client.task.impl.dto.FailureRequestDto;
import org.camunda.bpm.client.task.impl.dto.LockRequestDto;
import org.camunda.bpm.client.task.impl.dto.SetVariablesRequestDto;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.TypedValues;

public class EngineClient {

    protected static final String EXTERNAL_TASK_RESOURCE_PATH = "/external-task";
    protected static final String EXTERNAL_TASK_PROCESS_RESOURCE_PATH = "/process-instance";
    protected static final String FETCH_AND_LOCK_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/fetchAndLock";
    public static final String ID_PATH_PARAM = "{id}";
    protected static final String ID_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/" + ID_PATH_PARAM;
    public static final String LOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/lock";
    public static final String EXTEND_LOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/extendLock";
    public static final String SET_VARIABLES_RESOURCE_PATH = EXTERNAL_TASK_PROCESS_RESOURCE_PATH + "/" + ID_PATH_PARAM + "/variables";
    public static final String UNLOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/unlock";
    public static final String COMPLETE_RESOURCE_PATH = ID_RESOURCE_PATH + "/complete";
    public static final String FAILURE_RESOURCE_PATH = ID_RESOURCE_PATH + "/failure";
    public static final String BPMN_ERROR_RESOURCE_PATH = ID_RESOURCE_PATH + "/bpmnError";
    public static final String NAME_PATH_PARAM = "{name}";
    public static final String EXECUTION_RESOURCE_PATH = "/execution";
    public static final String EXECUTION_ID_RESOURCE_PATH = EXECUTION_RESOURCE_PATH + "/" + ID_PATH_PARAM;
    public static final String GET_LOCAL_VARIABLE = EXECUTION_ID_RESOURCE_PATH + "/localVariables/" + NAME_PATH_PARAM;
    public static final String GET_LOCAL_BINARY_VARIABLE = GET_LOCAL_VARIABLE + "/data";

    protected String baseUrl;
    protected String workerId;
    protected int maxTasks;
    protected boolean usePriority;
    protected OrderingConfig orderingConfig;
    protected Long asyncResponseTimeout;
    protected RequestExecutor engineInteraction;
    protected TypedValues typedValues;

    public EngineClient(String workerId, int maxTasks, Long asyncResponseTimeout, String baseUrl,
                        RequestExecutor engineInteraction) {
        this(workerId, maxTasks, asyncResponseTimeout, baseUrl, engineInteraction, true, OrderingConfig.empty());
    }

    public EngineClient(String workerId, int maxTasks, Long asyncResponseTimeout, String baseUrl,
                        RequestExecutor engineInteraction, boolean usePriority, OrderingConfig orderingConfig) {
        this.workerId = workerId;
        this.asyncResponseTimeout = asyncResponseTimeout;
        this.maxTasks = maxTasks;
        this.usePriority = usePriority;
        this.engineInteraction = engineInteraction;
        this.baseUrl = baseUrl;
        this.orderingConfig = orderingConfig;
    }

    public List<ExternalTask> fetchAndLock(List<TopicRequestDto> topics) throws Exception {
        FetchAndLockRequestDto payload = new FetchAndLockRequestDto(workerId, maxTasks, asyncResponseTimeout,
                topics, usePriority, orderingConfig);

        String resourceUrl = baseUrl + FETCH_AND_LOCK_RESOURCE_PATH;
        ExternalTask[] externalTasks = engineInteraction.postRequest(resourceUrl, payload, ExternalTaskImpl[].class);
        return Arrays.asList(externalTasks);
    }

    public void lock(String taskId, long lockDuration) throws Exception {
        LockRequestDto payload = new LockRequestDto(workerId, lockDuration);
        String resourcePath = LOCK_RESOURCE_PATH.replace(ID_PATH_PARAM, URLEncoder.encode(taskId, "UTF-8"));
        String resourceUrl = baseUrl + resourcePath;
        engineInteraction.postRequest(resourceUrl, payload, Void.class);
    }

    public void unlock(String taskId) throws Exception {
        String resourcePath = UNLOCK_RESOURCE_PATH.replace(ID_PATH_PARAM, URLEncoder.encode(taskId, "UTF-8"));
        String resourceUrl = baseUrl + resourcePath;
        engineInteraction.postRequest(resourceUrl, null, Void.class);
    }

    public void complete(String taskId, Map<String, Object> variables, Map<String, Object> localVariables) throws Exception {
        if (typedValues == null) {
            throw new IllegalStateException("TypedValues must be set before calling complete method.");
        }

        Map<String, TypedValueField> typedValueDtoMap = typedValues.serializeVariables(variables);
        Map<String, TypedValueField> localTypedValueDtoMap = typedValues.serializeVariables(localVariables);

        CompleteRequestDto payload = new CompleteRequestDto(workerId, typedValueDtoMap, localTypedValueDtoMap);
        String resourcePath = COMPLETE_RESOURCE_PATH.replace(ID_PATH_PARAM, URLEncoder.encode(taskId, "UTF-8"));
        String resourceUrl = baseUrl + resourcePath;
        engineInteraction.postRequest(resourceUrl, payload, Void.class);
    }

    public void setVariables(String processId, Map<String, Object> variables) throws Exception {
        if (typedValues == null) {
            throw new IllegalStateException("TypedValues must be set before calling setVariables method.");
        }

        Map<String, TypedValueField> typedValueDtoMap = typedValues.serializeVariables(variables);
        SetVariablesRequestDto payload = new SetVariablesRequestDto(workerId, typedValueDtoMap);
        String resourcePath = SET_VARIABLES_RESOURCE_PATH.replace(ID_PATH_PARAM, URLEncoder.encode(processId, "UTF-8"));
        String resourceUrl = baseUrl + resourcePath;
        engineInteraction.postRequest(resourceUrl, payload, Void.class);
    }

    public void failure(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout,
                        Map<String, Object> variables, Map<String, Object> localVariables) throws Exception {
        if (typedValues == null) {
            throw new IllegalStateException("TypedValues must be set before calling failure method.");
        }

        Map<String, TypedValueField> typedValueDtoMap = typedValues.serializeVariables(variables);
        Map<String, TypedValueField> localTypedValueDtoMap = typedValues.serializeVariables(localVariables);

        FailureRequestDto payload = new FailureRequestDto(workerId, errorMessage, errorDetails, retries, retryTimeout,
                typedValueDtoMap, localTypedValueDtoMap);
        String resourcePath = FAILURE_RESOURCE_PATH.replace(ID_PATH_PARAM, URLEncoder.encode(taskId, "UTF-8"));
        String resourceUrl = baseUrl + resourcePath;
        engineInteraction.postRequest(resourceUrl, payload, Void.class);
    }

    public void bpmnError(String taskId, String errorCode, String errorMessage, Map<String, Object> variables)
            throws Exception {
        if (typedValues == null) {
            throw new IllegalStateException("TypedValues must be set before calling bpmnError method.");
        }

        Map<String, TypedValueField> typeValueDtoMap = typedValues.serializeVariables(variables);
        
