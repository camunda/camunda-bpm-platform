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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Roman Smirnov
 *
 */
public class CaseSentryPartQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, CaseSentryPartQueryProperty> properties = new HashMap<String, CaseSentryPartQueryProperty>();

  public static final CaseExecutionQueryProperty CASE_SENTRY_PART_ID = new CaseExecutionQueryProperty("RES.ID_");
  public static final CaseExecutionQueryProperty CASE_INSTANCE_ID = new CaseExecutionQueryProperty("RES.CASE_INST_ID_");
  public static final CaseExecutionQueryProperty CASE_EXECUTION_ID = new CaseExecutionQueryProperty("RES.CASE_EXEC_ID");
  public static final CaseExecutionQueryProperty SENTRY_ID = new CaseExecutionQueryProperty("RES.SENTRY_ID_");
  public static final CaseExecutionQueryProperty SOURCE = new CaseExecutionQueryProperty("RES.SOURCE");

  private String name;

  public CaseSentryPartQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static CaseSentryPartQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
