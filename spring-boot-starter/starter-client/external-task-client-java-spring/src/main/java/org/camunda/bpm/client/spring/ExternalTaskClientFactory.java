package org.camunda.bpm.client.spring;

import lombok.Getter;
import lombok.Setter;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ExternalTaskClientFactory implements FactoryBean<ExternalTaskClient>, InitializingBean {

    @Getter
    @Setter
    private String baseUrl;
    @Getter
    @Setter
    private String id;
    @Getter
    private List<ClientRequestInterceptor> clientRequestInterceptors = new ArrayList<>();

    @Getter
    @Setter
    private Integer maxTasks;
    @Getter
    @Setter
    private String workerId;
    @Getter
    @Setter
    private Long asyncResponseTimeout;
    @Getter
    @Setter
    private Boolean autoFetchingEnabled;
    @Getter
    @Setter
    private Long lockDuration;
    @Getter
    @Setter
    private String dateFormat;
    @Getter
    @Setter
    private String defaultSerializationFormat;
    @Getter
    private List<BackoffStrategy> backoffStrategies = new ArrayList<>();

    private ExternalTaskClient externalTaskClient;

    @Override
    public ExternalTaskClient getObject() throws Exception {
        if (externalTaskClient == null) {
            ExternalTaskClientBuilder taskClientBuilder = ExternalTaskClient.create().baseUrl(baseUrl).workerId(workerId);
            addClientRequestInterceptors(taskClientBuilder);
            addClientBackoffStrategy(taskClientBuilder);
            if (maxTasks != null) {
                taskClientBuilder.maxTasks(maxTasks);
            }
            if (asyncResponseTimeout != null) {
                taskClientBuilder.asyncResponseTimeout(asyncResponseTimeout);
            }
            if (autoFetchingEnabled != null && autoFetchingEnabled == false) {
                taskClientBuilder.disableAutoFetching();
            }
            if (lockDuration != null) {
                taskClientBuilder.lockDuration(lockDuration);
            }
            if (dateFormat != null) {
                taskClientBuilder.dateFormat(dateFormat);
            }
            if (defaultSerializationFormat != null) {
                taskClientBuilder.defaultSerializationFormat(defaultSerializationFormat);
            }
            externalTaskClient = taskClientBuilder.build();
        }
        return externalTaskClient;
    }

    protected void addClientRequestInterceptors(ExternalTaskClientBuilder taskClientBuilder) {
        clientRequestInterceptors.stream().filter(filterAnyAndClientIdAccepting()).forEach(taskClientBuilder::addInterceptor);
    }

    protected void addClientBackoffStrategy(ExternalTaskClientBuilder taskClientBuilder) {
        Optional<BackoffStrategy> strategy = backoffStrategies.stream().filter(filterClientIdAccepting()).findFirst();
        if (!strategy.isPresent()) {
            strategy = backoffStrategies.stream().filter(isIdAcceptingInstance().negate()).findFirst();
        }
        strategy.ifPresent(taskClientBuilder::backoffStrategy);
    }

    @SuppressWarnings("unchecked")
    protected <T> Predicate<T> filterAnyAndClientIdAccepting() {
        return (Predicate<T>) isIdAcceptingInstance().negate().or(isIdAcceptingInstance().and(isAcceptingId()));

    }

    @SuppressWarnings("unchecked")
    protected <T> Predicate<T> filterClientIdAccepting() {
        return (Predicate<T>) isIdAcceptingInstance().and(isAcceptingId());

    }

    protected <T> Predicate<T> isAcceptingId() {
        return clientIdAccepting -> ((ClientIdAccepting) clientIdAccepting).accepts(getId());
    }

    protected <T> Predicate<T> isIdAcceptingInstance() {
        return clientIdAccepting -> clientIdAccepting instanceof ClientIdAccepting;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(baseUrl, "baseUrl must not be 'null'");
    }

    @Override
    public Class<ExternalTaskClient> getObjectType() {
        return ExternalTaskClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Autowired(required = false)
    public void setClientRequestInterceptors(List<ClientRequestInterceptor> clientRequestInterceptors) {
        this.clientRequestInterceptors = CollectionUtils.isEmpty(clientRequestInterceptors) ? new ArrayList<>() : clientRequestInterceptors;
    }

    @Autowired(required = false)
    public void setClientBackoffStrategies(List<BackoffStrategy> backoffStrategies) {
        this.backoffStrategies = CollectionUtils.isEmpty(backoffStrategies) ? new ArrayList<>() : backoffStrategies;
    }

}
