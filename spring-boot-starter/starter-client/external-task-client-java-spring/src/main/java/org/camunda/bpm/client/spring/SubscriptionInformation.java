package org.camunda.bpm.client.spring;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class SubscriptionInformation {

    public static final long DEFAULT_LOCK_DURATION = 1000;
    public static final boolean DEFAULT_AUTO_SUBSCRIBE = true;
    public static final boolean DEFAULT_AUTO_OPEN = true;

    private final String topicName;
    private long lockDuration = DEFAULT_LOCK_DURATION;
    private boolean autoSubscribe = DEFAULT_AUTO_SUBSCRIBE;
    private boolean autoOpen = DEFAULT_AUTO_OPEN;
    private String[] externalTaskClientIds = new String[]{};
    private List<String> variableNames;
    private String businessKey;
    private String processDefinitionId;
    private List<String> processDefinitionIdIn = new ArrayList<>();
    private String processDefinitionKey;
    private List<String> processDefinitionKeyIn = new ArrayList<>();
    private boolean withoutTenantId;
    private List<String> tenantIdIn = new ArrayList<>();

    public void setVariableNames(String... variableNames) {
        if (variableNames == null) {
            this.variableNames = null;
        } else {
            this.variableNames = Arrays.asList(variableNames);
        }
    }
}
