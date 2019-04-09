package org.camunda.bpm.client.spring.interceptor;

import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.spring.ClientIdAccepting;

public interface ClientIdAcceptingClientRequestInterceptor extends ClientRequestInterceptor, ClientIdAccepting {

}
