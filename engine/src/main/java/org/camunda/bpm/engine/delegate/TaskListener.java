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

package org.camunda.bpm.engine.delegate;

/**
 * Listener interface implemented by user code which wants to be notified when a property of a task changes.
 *
 * <p>The following Task Events are supported:
 * <ul>
 * <li>{@link #EVENTNAME_CREATE}</li>
 * <li>{@link #EVENTNAME_ASSIGNMENT}</li>
 * <li>{@link #EVENTNAME_COMPLETE}</li>
 * <li>{@link #EVENTNAME_DELETE}</li>
 * </ul>
 * </p>
 *
 * @author Tom Baeyens
 */
public interface TaskListener {

  String EVENTNAME_CREATE = "create";
  String EVENTNAME_ASSIGNMENT = "assignment";
  String EVENTNAME_COMPLETE = "complete";
  String EVENTNAME_DELETE = "delete";

  void notify(DelegateTask delegateTask);

}
