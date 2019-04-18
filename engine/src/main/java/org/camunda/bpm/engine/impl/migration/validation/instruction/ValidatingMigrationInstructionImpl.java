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
package org.camunda.bpm.engine.impl.migration.validation.instruction;

import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class ValidatingMigrationInstructionImpl implements ValidatingMigrationInstruction {

  protected ActivityImpl sourceActivity;
  protected ActivityImpl targetActivity;
  protected boolean updateEventTrigger = false;

  public ValidatingMigrationInstructionImpl(ActivityImpl sourceActivity, ActivityImpl targetActivity, boolean updateEventTrigger) {
    this.sourceActivity = sourceActivity;
    this.targetActivity = targetActivity;
    this.updateEventTrigger = updateEventTrigger;
  }

  public ActivityImpl getSourceActivity() {
    return sourceActivity;
  }

  public ActivityImpl getTargetActivity() {
    return targetActivity;
  }

  @Override
  public boolean isUpdateEventTrigger() {
    return updateEventTrigger;
  }

  public MigrationInstruction toMigrationInstruction() {
    return new MigrationInstructionImpl(sourceActivity.getId(), targetActivity.getId(), updateEventTrigger);
  }

  public String toString() {
    return "ValidatingMigrationInstructionImpl{" +
      "sourceActivity=" + sourceActivity +
      ", targetActivity=" + targetActivity +
      '}';
  }

}
