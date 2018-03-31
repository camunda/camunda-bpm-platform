package org.camunda.bpm.client.spring.boot.starter.interceptor;

import java.util.Optional;

import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

public interface IdAwareClientRequestInterceptor extends ClientRequestInterceptor {

  Optional<String> getId();

}
