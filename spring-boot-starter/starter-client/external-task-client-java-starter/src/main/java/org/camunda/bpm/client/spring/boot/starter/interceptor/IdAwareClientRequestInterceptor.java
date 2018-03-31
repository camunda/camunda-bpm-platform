package org.camunda.bpm.client.spring.boot.starter.interceptor;

import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

public interface IdAwareClientRequestInterceptor extends ClientRequestInterceptor {

  String getId();

}
