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
package org.camunda.bpm.integrationtest.functional.spin.dataformat;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * @author Thorben Lindhauer
 *
 */
public class ImplicitObjectValueUpdateDelegate implements JavaDelegate {

  public static final String VARIABLE_NAME = "var";
  public static final long ONE_DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

  public void execute(DelegateExecution execution) throws Exception {
    JsonSerializable variable = (JsonSerializable) execution.getVariable(VARIABLE_NAME);

    addADay(variable);  // implicit update, i.e. no setVariable call

  }

  public static void addADay(JsonSerializable jsonSerializable) {
    Date newDate = new Date(jsonSerializable.getDateProperty().getTime() + ONE_DAY_IN_MILLIS);
    jsonSerializable.setDateProperty(newDate);
  }

}
