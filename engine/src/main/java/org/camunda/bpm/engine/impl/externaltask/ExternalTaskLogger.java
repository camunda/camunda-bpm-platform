/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.externaltask;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Represents the logger for the external task.
 * 
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ExternalTaskLogger extends ProcessEngineLogger {
  
  /**
   * Logs that the priority could not be determined in the given context.
   * 
   * @param execution the context that is used for determining the priority
   * @param value the default value
   * @param e the exception which was catched
   */
  public void couldNotDeterminePriority(ExecutionEntity execution, Object value, ProcessEngineException e) {
    logWarn(
        "001",
        "Could not determine priority for external task created in context of execution {}. Using default priority {}",
        execution, value, e);
  }
}
