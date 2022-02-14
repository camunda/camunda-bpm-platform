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
package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;

/**
 * The BPMN startEvent element
 *
 * @author Sebastian Menski
 *
 */
public interface StartEvent extends CatchEvent {

  StartEventBuilder builder();

  boolean isInterrupting();

  void setInterrupting(boolean isInterrupting);

  /** camunda extensions */

  /**
   * @deprecated use isCamundaAsyncBefore() instead.
   */
  @Deprecated
  boolean isCamundaAsync();

  /**
   * @deprecated use setCamundaAsyncBefore(isCamundaAsyncBefore) instead.
   */
  @Deprecated
  void setCamundaAsync(boolean isCamundaAsync);

  String getCamundaFormHandlerClass();

  void setCamundaFormHandlerClass(String camundaFormHandlerClass);

  String getCamundaFormKey();

  void setCamundaFormKey(String camundaFormKey);

  String getCamundaFormRef();

  void setCamundaFormRef(String camundaFormRef);

  String getCamundaFormRefBinding();

  void setCamundaFormRefBinding(String camundaFormRefBinding);

  String getCamundaFormRefVersion();

  void setCamundaFormRefVersion(String camundaFormRefVersion);

  String getCamundaInitiator();

  void setCamundaInitiator(String camundaInitiator);
}
