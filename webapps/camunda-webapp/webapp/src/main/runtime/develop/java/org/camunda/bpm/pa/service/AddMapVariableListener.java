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
package org.camunda.bpm.pa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * @author Daniel Meyer
 *
 */
public class AddMapVariableListener implements TaskListener {
  
  public void notify(DelegateTask task) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("demo", "Demo");
    map.put("john", "Jonny Boy");
    map.put("peter", "Peter Meter");
    map.put("mary", "Mary");
    task.setVariable("namesMap", map);
  }

}
