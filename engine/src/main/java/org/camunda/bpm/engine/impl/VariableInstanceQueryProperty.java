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

/**
 * @author roman.smirnov
 */
public interface VariableInstanceQueryProperty {

  public static final QueryProperty VARIABLE_NAME = new QueryPropertyImpl("NAME_");
  public static final QueryProperty VARIABLE_TYPE = new QueryPropertyImpl("TYPE_");
  public static final QueryProperty ACTIVITY_INSTANCE_ID = new QueryPropertyImpl("ACT_INST_ID_");
  public static final QueryProperty EXECUTION_ID = new QueryPropertyImpl("EXECUTION_ID_");
  public static final QueryProperty TASK_ID = new QueryPropertyImpl("TASK_ID_");
  public static final QueryProperty CASE_EXECUTION_ID = new QueryPropertyImpl("CASE_EXECUTION_ID_");
  public static final QueryProperty CASE_INSTANCE_ID = new QueryPropertyImpl("CASE_INST_ID_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");

  public static final QueryProperty TEXT = new QueryPropertyImpl("TEXT_");
  public static final QueryProperty TEXT_AS_LOWER = new QueryPropertyImpl("TEXT_", "LOWER");
  public static final QueryProperty DOUBLE = new QueryPropertyImpl("DOUBLE_");
  public static final QueryProperty LONG = new QueryPropertyImpl("LONG_");

}
