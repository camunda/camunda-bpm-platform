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
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Roman Smirnov
 *
 */
public interface CaseSentryPartQueryProperty {

  public static final QueryProperty CASE_SENTRY_PART_ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty CASE_INSTANCE_ID = new QueryPropertyImpl("CASE_INST_ID_");
  public static final QueryProperty CASE_EXECUTION_ID = new QueryPropertyImpl("CASE_EXEC_ID");
  public static final QueryProperty SENTRY_ID = new QueryPropertyImpl("SENTRY_ID_");
  public static final QueryProperty SOURCE = new QueryPropertyImpl("SOURCE");
}
