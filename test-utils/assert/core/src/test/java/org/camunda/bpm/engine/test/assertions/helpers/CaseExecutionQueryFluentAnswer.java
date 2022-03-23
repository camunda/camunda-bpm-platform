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
package org.camunda.bpm.engine.test.assertions.helpers;

import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Default Answer for CaseExecutionQuery mocks that returns the mock as default answer.
 * Eases mocking of this class as most of its methods support the fluent API and therefore return the invoked instance.
 *
 */
public class CaseExecutionQueryFluentAnswer implements Answer<CaseExecutionQuery> {

  @Override
  public CaseExecutionQuery answer(InvocationOnMock invocationOnMock) throws Throwable {
    if (invocationOnMock.getMethod().getReturnType().isAssignableFrom(CaseExecutionQuery.class)) {
      return (CaseExecutionQuery) invocationOnMock.getMock();
    } else {
      return null;
    }
  }
}
