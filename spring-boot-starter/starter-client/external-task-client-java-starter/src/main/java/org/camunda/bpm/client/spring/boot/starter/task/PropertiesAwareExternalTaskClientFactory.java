package org.camunda.bpm.client.spring.boot.starter.task;

import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.BasicAuthProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.Client;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class PropertiesAwareExternalTaskClientFactory extends ExternalTaskClientFactory {

    @Autowired
    private CamundaBpmClientProperties camundaBpmClientProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        camundaBpmClientProperties.getBaseUrl(getId()).ifPresent(this::setBaseUrl);
        applyClientProperties(camundaBpmClientProperties.getClient(getId()));
        addBasicAuthInterceptor();
        super.afterPropertiesSet();
    }

    protected void addBasicAuthInterceptor() {
        camundaBpmClientProperties.getClient(getId()).map(Client::getBasicAuth).filter(BasicAuthProperties::isEnabled)
                .ifPresent(basicAuth -> getClientRequestInterceptors().add(new BasicAuthProvider(basicAuth.getUsername(), basicAuth.getPassword())));
    }

    protected void applyClientProperties(Optional<Client> client) {
        client.ifPresent(c -> {
            if (c.getMaxTasks() != null) {
                setMaxTasks(c.getMaxTasks());
            }
            if (c.getWorkerId() != null) {
                setWorkerId(c.getWorkerId());
            }
            if (c.getAsyncResponseTimeout() != null) {
                setAsyncResponseTimeout(c.getAsyncResponseTimeout());
            }
            if (c.getAutoFetchingEnabled() != null) {
                setAutoFetchingEnabled(c.getAutoFetchingEnabled());
            }
            if (c.getLockDuration() != null) {
                setLockDuration(c.getLockDuration());
            }
            if (c.getDateFormat() != null) {
                setDateFormat(c.getDateFormat());
            }
            if (c.getDefaultSerializationFormat() != null) {
                setDefaultSerializationFormat(c.getDefaultSerializationFormat());
            }
        });
    }

}
