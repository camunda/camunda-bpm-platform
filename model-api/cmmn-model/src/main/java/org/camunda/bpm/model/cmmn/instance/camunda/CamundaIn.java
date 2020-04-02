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
package org.camunda.bpm.model.cmmn.instance.camunda;

import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance;


/**
 * The CMMN in camunda extension element
 *
 * @author Sebastian Menski
 * @author Roman Smirnov
 *
 */
public interface CamundaIn extends CmmnModelElementInstance {

  String getCamundaSource();

  void setCamundaSource(String camundaSource);

  String getCamundaSourceExpression();

  void setCamundaSourceExpression(String camundaSourceExpression);

  String getCamundaVariables();

  void setCamundaVariables(String camundaVariables);

  String getCamundaTarget();

  void setCamundaTarget(String camundaTarget);

  String getCamundaBusinessKey();

  void setCamundaBusinessKey(String camundaBusinessKey);

  boolean getCamundaLocal();

  void setCamundaLocal(boolean local);

}
