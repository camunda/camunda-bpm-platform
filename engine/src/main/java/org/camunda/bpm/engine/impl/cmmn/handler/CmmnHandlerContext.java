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
package org.camunda.bpm.engine.impl.cmmn.handler;

import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.handler.HandlerContext;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;

/**
 * <p>This context contains necessary information (like caseDefinition)
 * to be accessed by a {@link CmmnHandlerContext}.</p>
 *
 * @author Roman Smirnov
 *
 */
public class CmmnHandlerContext implements HandlerContext {

  protected ExpressionManager expressionManager;
  protected CmmnCaseDefinition caseDefinition;
  protected CmmnModelInstance model;
  protected CmmnActivity parent;
  protected Deployment deployment;

  public CmmnHandlerContext() {
  }

  public CmmnModelInstance getModel() {
    return model;
  }

  public void setModel(CmmnModelInstance model) {
    this.model = model;
  }

  public CmmnCaseDefinition getCaseDefinition() {
    return caseDefinition;
  }

  public void setCaseDefinition(CmmnCaseDefinition caseDefinition) {
    this.caseDefinition = caseDefinition;
  }

  public CmmnActivity getParent() {
    return parent;
  }

  public void setParent(CmmnActivity parent) {
    this.parent = parent;
  }

  public Deployment getDeployment() {
    return deployment;
  }

  public void setDeployment(Deployment deployment) {
    this.deployment = deployment;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

}
