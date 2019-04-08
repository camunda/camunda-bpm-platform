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

import java.util.List;

import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.spi.ConnectorInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;

/**
 * A simple invocation implementation routing a request through a chain of interceptors.
 * Implementations must implement the {@link #invokeTarget()} method in order to implement
 * the actual request execution / target invocation.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractRequestInvocation<T> implements ConnectorInvocation {

  protected T target;

  protected int currentIndex;

  protected List<ConnectorRequestInterceptor> interceptorChain;

  protected ConnectorRequest<?> request;

  public AbstractRequestInvocation(T target, ConnectorRequest<?> request, List<ConnectorRequestInterceptor> interceptorChain) {
    this.target = target;
    this.request = request;
    this.interceptorChain = interceptorChain;
    currentIndex = -1;
  }

  public T getTarget() {
    return target;
  }

  public ConnectorRequest<?> getRequest() {
    return request;
  }

  public Object proceed() throws Exception {
    currentIndex++;
    if(interceptorChain.size() > currentIndex) {
      return interceptorChain.get(currentIndex).handleInvocation(this);

    } else {
      return invokeTarget();

    }
  }

  public abstract Object invokeTarget() throws Exception;

}
