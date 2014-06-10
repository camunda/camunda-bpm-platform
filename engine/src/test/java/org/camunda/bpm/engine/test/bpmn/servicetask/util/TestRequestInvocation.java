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
package org.camunda.bpm.engine.test.bpmn.servicetask.util;

import java.util.List;

import org.camunda.bpm.connect.ConnectorRequest;
import org.camunda.bpm.connect.impl.AbstractRequestInvocation;
import org.camunda.bpm.connect.interceptor.RequestInterceptor;

/**
 * @author Daniel Meyer
 *
 */
public class TestRequestInvocation extends AbstractRequestInvocation<Object> {

  public TestRequestInvocation(Object target, ConnectorRequest<?> request, List<RequestInterceptor> interceptorChain) {
    super(target, request, interceptorChain);
  }

  public Object invokeTarget() throws Exception {
    return null;
  }

}
