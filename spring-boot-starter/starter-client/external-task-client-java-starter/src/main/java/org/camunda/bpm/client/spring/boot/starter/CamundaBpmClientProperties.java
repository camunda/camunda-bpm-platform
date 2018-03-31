package org.camunda.bpm.client.spring.boot.starter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ConfigurationProperties(prefix = "camunda.bpm.client")
@Data
public class CamundaBpmClientProperties {

  protected BasicAuthProperties basicAuth = new BasicAuthProperties();
  protected Map<String, SubscriptionProperties> subscriptions = new HashMap<>();
  protected Map<String, Client> clients = new HashMap<>();

  public String getBaseUrl() {
    Client client = clients.get("");
    return client == null ? null : client.getBaseUrl();
  }

  public void setBaseUrl(String baseUrl) {
    clients.put("", new Client(baseUrl));
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
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Client {
    protected String baseUrl;
  }

}
