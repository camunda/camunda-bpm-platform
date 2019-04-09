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
package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;

/**
 * Interceptor responsible for handling calls to 'user code'. User code
 * represents external Java code (e.g. services and listeners) invoked by
 * activity. The following is a list of classes that represent user code:
 * <ul>
 * <li>{@link org.camunda.bpm.engine.delegate.JavaDelegate}</li>
 * <li>{@link org.camunda.bpm.engine.delegate.CaseExecutionListener}</li>
 * <li>{@link org.camunda.bpm.engine.delegate.ExecutionListener}</li>
 * <li>{@link org.camunda.bpm.engine.delegate.Expression}</li>
 * <li>{@link org.camunda.bpm.engine.delegate.TaskListener}</li>
 * <li>{@link DmnDecision}</li>
 * </ul>
 *
 * The interceptor is passed in an instance of {@link DelegateInvocation}.
 * Implementations are responsible for calling
 * {@link DelegateInvocation#proceed()}.
 *
 * @author Daniel Meyer
 */
public interface DelegateInterceptor {

  void handleInvocation(DelegateInvocation invocation) throws Exception;

}
