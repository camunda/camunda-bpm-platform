/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskActivityBehavior extends TaskActivityBehavior {

  public void started(CmmnActivityExecution execution) throws Exception {
    // TODO: implement the following steps:

    // 1. Step: start process instance to the given id and add
    // parameters (maybe the starting of a new process instance
    // should happen async!?)

    // 2. Step: If "isBlocking == false" -> complete this case execution

  }

  public void onCompletion(CmmnActivityExecution execution) {
    // if "isBlocking == false" then there is nothing to check,
    // but if "isBlocking == true" then we have to check,
    // whether the started process instance is finished.
  }


  public void completed(CmmnActivityExecution execution) {
    // get defined parameters from process instance to case instance
  }

}
