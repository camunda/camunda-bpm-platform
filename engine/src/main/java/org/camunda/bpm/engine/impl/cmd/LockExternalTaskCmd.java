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
package org.camunda.bpm.engine.impl.cmd;

import java.util.Date;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class LockExternalTaskCmd extends HandleExternalTaskCmd {

  protected long lockDuration;

  public LockExternalTaskCmd(String externalTaskId, String workerId, long lockDuration) {
    super(externalTaskId, workerId);
    this.lockDuration = lockDuration;
  }

  @Override
  protected void execute(ExternalTaskEntity externalTask) {
    externalTask.lock(workerId, lockDuration);
  }

  @Override
  public String getErrorMessageOnWrongWorkerAccess() {
    return "External Task " + externalTaskId + " cannot be locked by worker '" + workerId;
  }

  /*
    Report a worker violation only if another worker has locked the task,
    and the lock expiration time is still not expired.
   */
  @Override
  protected boolean validateWorkerViolation(ExternalTaskEntity externalTask) {
    String existingWorkerId = externalTask.getWorkerId();
    Date existingLockExpirationTime = externalTask.getLockExpirationTime();

    // check if another worker is attempting to lock the same task
    boolean workerValidation = existingWorkerId != null && !workerId.equals(existingWorkerId);
    // and check if an existing lock is already expired
    boolean lockValidation = existingLockExpirationTime != null
        && !ClockUtil.getCurrentTime().after(existingLockExpirationTime);

    return workerValidation && lockValidation;
  }

  @Override
  protected void validateInput() {
    super.validateInput();
    EnsureUtil.ensurePositive(BadUserRequestException.class, "lockDuration", lockDuration);
  }
}