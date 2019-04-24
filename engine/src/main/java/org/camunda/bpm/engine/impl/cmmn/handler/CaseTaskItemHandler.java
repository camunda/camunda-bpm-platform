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

import org.camunda.bpm.engine.impl.cmmn.behavior.CaseTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * @author Roman Smirnov
 *
 */
public class CaseTaskItemHandler extends ProcessOrCaseTaskItemHandler {

  protected CmmnActivityBehavior getActivityBehavior() {
    return new CaseTaskActivityBehavior();
  }

  protected CaseTask getDefinition(CmmnElement element) {
    return (CaseTask) super.getDefinition(element);
  }

  protected String getDefinitionKey(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    CaseTask definition = getDefinition(element);

    return definition.getCase();
  }

  protected String getBinding(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    CaseTask definition = getDefinition(element);

    return definition.getCamundaCaseBinding();
  }

  protected String getVersion(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    CaseTask definition = getDefinition(element);

    return definition.getCamundaCaseVersion();
  }

  protected String getTenantId(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    CaseTask definition = getDefinition(element);

    return definition.getCamundaCaseTenantId();
  }

}
