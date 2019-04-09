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
package org.camunda.bpm.engine.impl.migration.validation.instance;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingCompensationEventSubscriptionInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingEventScopeInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;

public class NoUnmappedLeafInstanceValidator implements
  MigratingActivityInstanceValidator,
  MigratingTransitionInstanceValidator,
  MigratingCompensationInstanceValidator {

  public void validate(MigratingActivityInstance migratingInstance, MigratingProcessInstance migratingProcessInstance, MigratingActivityInstanceValidationReportImpl instanceReport) {
    if (isInvalid(migratingInstance)) {
      instanceReport.addFailure("There is no migration instruction for this instance's activity");
    }
  }

  @Override
  public void validate(MigratingTransitionInstance migratingInstance, MigratingProcessInstance migratingProcessInstance,
      MigratingTransitionInstanceValidationReportImpl instanceReport) {
    if (isInvalid(migratingInstance)) {
      instanceReport.addFailure("There is no migration instruction for this instance's activity");
    }
  }

  @Override
  public void validate(MigratingCompensationEventSubscriptionInstance migratingInstance, MigratingProcessInstance migratingProcessInstance,
      MigratingActivityInstanceValidationReportImpl ancestorInstanceReport) {
    if (isInvalid(migratingInstance)) {
      ancestorInstanceReport.addFailure(
            "Cannot migrate subscription for compensation handler '" + migratingInstance.getSourceScope().getId() + "'. "
          + "There is no migration instruction for the compensation boundary event");
    }
  }

  @Override
  public void validate(MigratingEventScopeInstance migratingInstance, MigratingProcessInstance migratingProcessInstance,
      MigratingActivityInstanceValidationReportImpl ancestorInstanceReport) {
    if (isInvalid(migratingInstance)) {
      ancestorInstanceReport.addFailure(
          "Cannot migrate subscription for compensation handler '" + migratingInstance.getEventSubscription().getSourceScope().getId() + "'. "
        + "There is no migration instruction for the compensation start event");
    }
  }

  protected boolean isInvalid(MigratingActivityInstance migratingInstance) {
    return hasNoInstruction(migratingInstance) && migratingInstance.getChildren().isEmpty();
  }

  protected boolean isInvalid(MigratingEventScopeInstance migratingInstance) {
    return hasNoInstruction(migratingInstance.getEventSubscription()) && migratingInstance.getChildren().isEmpty();
  }

  protected boolean isInvalid(MigratingTransitionInstance migratingInstance) {
    return hasNoInstruction(migratingInstance);
  }

  protected boolean isInvalid(MigratingCompensationEventSubscriptionInstance migratingInstance) {
    return hasNoInstruction(migratingInstance);
  }

  protected boolean hasNoInstruction(MigratingProcessElementInstance migratingInstance) {
    return migratingInstance.getMigrationInstruction() == null;
  }
}
