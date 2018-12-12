/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;

/**
 * Represents the command to set the priority of an existing external task.
 * 
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class SetExternalTaskPriorityCmd extends ExternalTaskCmd {
  
  /**
   * The priority that should set on the external task.
   */
  protected long priority;

  public SetExternalTaskPriorityCmd(String externalTaskId, long priority) {
    super(externalTaskId);
    this.priority = priority;
  }

  @Override
  protected void execute(ExternalTaskEntity externalTask) {
    externalTask.setPriority(priority);    
  }

  @Override
  protected void validateInput() {
  }
}
