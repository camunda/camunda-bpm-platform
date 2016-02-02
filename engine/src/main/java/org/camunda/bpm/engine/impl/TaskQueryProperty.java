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

package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.query.QueryProperty;
import org.camunda.bpm.engine.task.TaskQuery;



/**
 * Contains the possible properties that can be used in a {@link TaskQuery}.
 *
 * @author Joram Barrez
 */
public interface TaskQueryProperty {

  public static final QueryProperty TASK_ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty NAME = new QueryPropertyImpl("NAME_");
  public static final QueryProperty NAME_CASE_INSENSITIVE = new QueryPropertyImpl("NAME_", "LOWER");
  public static final QueryProperty DESCRIPTION = new QueryPropertyImpl("DESCRIPTION_");
  public static final QueryProperty PRIORITY = new QueryPropertyImpl("PRIORITY_");
  public static final QueryProperty ASSIGNEE = new QueryPropertyImpl("ASSIGNEE_");
  public static final QueryProperty CREATE_TIME = new QueryPropertyImpl("CREATE_TIME_");
  public static final QueryProperty PROCESS_INSTANCE_ID = new QueryPropertyImpl("PROC_INST_ID_");
  public static final QueryProperty CASE_INSTANCE_ID = new QueryPropertyImpl("CASE_INST_ID_");
  public static final QueryProperty EXECUTION_ID = new QueryPropertyImpl("EXECUTION_ID_");
  public static final QueryProperty CASE_EXECUTION_ID = new QueryPropertyImpl("CASE_EXECUTION_ID_");
  public static final QueryProperty DUE_DATE = new QueryPropertyImpl("DUE_DATE_");
  public static final QueryProperty FOLLOW_UP_DATE = new QueryPropertyImpl("FOLLOW_UP_DATE_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");

}
