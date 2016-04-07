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

import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * Contains the possible properties which can be used in a {@link HistoricCaseActivityInstanceQuery}.
 *
 * @author Sebastian Menski
 */
public interface HistoricCaseActivityInstanceQueryProperty {

  public static final QueryProperty HISTORIC_CASE_ACTIVITY_INSTANCE_ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty CASE_INSTANCE_ID = new QueryPropertyImpl("CASE_INST_ID_");
  public static final QueryProperty CASE_ACTIVITY_ID = new QueryPropertyImpl("CASE_ACT_ID_");
  public static final QueryProperty CASE_ACTIVITY_NAME = new QueryPropertyImpl("CASE_ACT_NAME_");
  public static final QueryProperty CASE_ACTIVITY_TYPE = new QueryPropertyImpl("CASE_ACT_TYPE_");
  public static final QueryProperty CASE_DEFINITION_ID = new QueryPropertyImpl("CASE_DEF_ID_");
  public static final QueryProperty CREATE = new QueryPropertyImpl("CREATE_TIME_");
  public static final QueryProperty END = new QueryPropertyImpl("END_TIME_");
  public static final QueryProperty DURATION = new QueryPropertyImpl("DURATION_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");

}
