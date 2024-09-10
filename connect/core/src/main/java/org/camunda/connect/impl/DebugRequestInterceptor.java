/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.connect.impl;

import org.camunda.connect.spi.ConnectorInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;
import org.camunda.connect.spi.ConnectorRequest;

/**
 * <p>
 *   A dummy debug connector, which saves the {@link ConnectorRequest} ({@link #getRequest()})
 *   and the raw request ({@link #getTarget()}) for debugging purpose.
 * </p>
 *
 * <p>
 *   The boolean constructor flag determines whether the request invocation should be continued
 *   or aborted after this interceptor. Also it is possible to add a response object which
 *   will returned without further passing the interceptor chain;
 * </p>
 *
 * @author Sebastian Menski
 */
public class DebugRequestInterceptor implements ConnectorRequestInterceptor {

  protected Object response;
  protected boolean proceed;

  private ConnectorRequest<?> request;
  private Object target;

  public DebugRequestInterceptor() {
    this(true);
  }

  public DebugRequestInterceptor(boolean proceed) {
    this.proceed = proceed;
  }

  public DebugRequestInterceptor(Object response) {
    this.response = response;
    this.proceed = false;
  }

  public Object handleInvocation(ConnectorInvocation invocation) throws Exception {
    request = invocation.getRequest();
    target = invocation.getTarget();
    if (proceed) {
      return invocation.proceed();
    }
    else {
      return response;
    }
  }

  public void setProceed(boolean proceed) {
    this.proceed = proceed;
  }

  public boolean isProceed() {
    return proceed;
  }

  public void setResponse(Object response) {
    this.response = response;
  }

  @SuppressWarnings("unchecked")
  public <T> T getResponse() {
    return (T) response;
  }

  @SuppressWarnings("unchecked")
  public <T extends ConnectorRequest<?>> T getRequest() {
    return (T) request;
  }

  @SuppressWarnings("unchecked")
  public <T> T getTarget() {
    return (T) target;
  }

}
