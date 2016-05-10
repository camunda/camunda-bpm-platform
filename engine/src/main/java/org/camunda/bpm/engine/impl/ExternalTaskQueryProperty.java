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
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTaskQueryProperty {

  public static final QueryProperty ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty LOCK_EXPIRATION_TIME = new QueryPropertyImpl("LOCK_EXP_TIME_");
  public static final QueryProperty PROCESS_INSTANCE_ID = new QueryPropertyImpl("PROC_INST_ID_");
  public static final QueryProperty PROCESS_DEFINITION_ID = new QueryPropertyImpl("PROC_DEF_ID_");
  public static final QueryProperty PROCESS_DEFINITION_KEY = new QueryPropertyImpl("PROC_DEF_KEY_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");
  public static final QueryProperty PRIORITY = new QueryPropertyImpl("PRIORITY_");

}
