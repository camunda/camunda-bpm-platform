/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.connect.impl;

import java.util.List;

import org.camunda.bpm.connect.ConnectorRequest;
import org.camunda.bpm.connect.interceptor.ConnectorInvocation;
import org.camunda.bpm.connect.interceptor.RequestInterceptor;

/**
 * A simple invocation implementation routing a request through a chain of interceptors.
 * Implementors must implement the {@link #invokeTarget()} method in order to implement
 * the actual request execution / target invocation.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractRequestInvocation<T> implements ConnectorInvocation {

  protected T target;

  protected int currentIndex;

  protected List<RequestInterceptor> interceptorChain;

  protected ConnectorRequest<?> request;

  public AbstractRequestInvocation(T target, ConnectorRequest<?> request, List<RequestInterceptor> interceptorChain) {
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
