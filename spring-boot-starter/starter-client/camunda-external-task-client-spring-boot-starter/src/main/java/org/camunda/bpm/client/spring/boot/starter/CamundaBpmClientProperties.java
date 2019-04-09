package org.camunda.bpm.client.spring.boot.starter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ConfigurationProperties(prefix = "camunda.bpm.client")
@Data
public class CamundaBpmClientProperties implements InitializingBean {

  protected String baseUrl;
  protected Integer maxTasks;
  protected String workerId;
  protected Long asyncResponseTimeout;
  protected Boolean autoFetchingEnabled;
  protected Long lockDuration;
  protected String dateFormat;
  protected String defaultSerializationFormat;
  protected BasicAuthProperties basicAuth = new BasicAuthProperties();
  protected Map<String, SubscriptionProperties> subscriptions = new HashMap<>();
  protected Map<String, Client> clients = new HashMap<>();

  @Override
  public void afterPropertiesSet() {
    clients.put("",
        new Client(baseUrl, basicAuth, maxTasks, workerId, asyncResponseTimeout, autoFetchingEnabled, lockDuration, dateFormat, defaultSerializationFormat));
  }

  public Optional<SubscriptionProperties> subscriptionInformationFor(String topic) {
    return Optional.ofNullable(subscriptions.get(topic));
  }

  public Optional<String> getBaseUrl(String id) {
    String baseUrl = null;
    Client client = clients.get(id);
    if (client != null) {
      baseUrl = client.getBaseUrl();
    }
    if (baseUrl == null) {
      baseUrl = this.getBaseUrl();
    }
    return Optional.ofNullable(baseUrl);
  }

  public Optional<Client> getClient(String id) {
    return Optional.ofNullable(clients.get(id));
  }

  @Data
  @ToString(exclude = { "password" })
  public static class BasicAuthProperties {
    protected boolean enabled = false;
    protected String username;
    protected String password;
  }

  @Data
  public static class SubscriptionProperties {
    private Long lockDuration;
    private Boolean autoSubscribe;
    private Boolean autoOpen;
    private String[] variableNames;
    private String businessKey;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Client {
    protected String baseUrl;
    protected BasicAuthProperties basicAuth = new BasicAuthProperties();
    protected Integer maxTasks;
    protected String workerId;
    protected Long asyncResponseTimeout;
    protected Boolean autoFetchingEnabled;
    protected Long lockDuration;
    protected String dateFormat;
    protected String defaultSerializationFormat;
  }

}
