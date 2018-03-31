package org.camunda.bpm.client.spring;

public interface IdAwareBaseUrlSupplier extends BaseUrlSupplier {

  String getBaseUrl();

  void setBaseUrl(String baseUrl);

  String getId();
}
