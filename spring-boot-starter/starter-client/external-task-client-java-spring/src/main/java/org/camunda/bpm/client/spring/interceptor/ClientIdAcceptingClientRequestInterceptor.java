package org.camunda.bpm.client.spring.interceptor;

import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

public interface ClientIdAcceptingClientRequestInterceptor extends ClientRequestInterceptor {

  boolean accepts(String id);

}
