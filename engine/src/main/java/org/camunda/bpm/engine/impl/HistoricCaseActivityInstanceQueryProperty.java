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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * Contains the possible properties which can be used in a {@link HistoricCaseActivityInstanceQuery}.
 *
 * @author Sebastian Menski
 */
public class HistoricCaseActivityInstanceQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;

  private static final Map<String, HistoricCaseActivityInstanceQueryProperty> properties = new HashMap<String, HistoricCaseActivityInstanceQueryProperty>();

  public static final HistoricCaseActivityInstanceQueryProperty HISTORIC_CASE_ACTIVITY_INSTANCE_ID = new HistoricCaseActivityInstanceQueryProperty("ID_");
  public static final HistoricCaseActivityInstanceQueryProperty CASE_INSTANCE_ID = new HistoricCaseActivityInstanceQueryProperty("CASE_INST_ID_");
  public static final HistoricCaseActivityInstanceQueryProperty CASE_ACTIVITY_ID = new HistoricCaseActivityInstanceQueryProperty("CASE_ACT_ID_");
  public static final HistoricCaseActivityInstanceQueryProperty CASE_ACTIVITY_NAME = new HistoricCaseActivityInstanceQueryProperty("CASE_ACT_NAME_");
  public static final HistoricCaseActivityInstanceQueryProperty CASE_ACTIVITY_TYPE = new HistoricCaseActivityInstanceQueryProperty("CASE_ACT_TYPE_");
  public static final HistoricCaseActivityInstanceQueryProperty CASE_DEFINITION_ID = new HistoricCaseActivityInstanceQueryProperty("CASE_DEF_ID_");
  public static final HistoricCaseActivityInstanceQueryProperty CREATE = new HistoricCaseActivityInstanceQueryProperty("CREATE_TIME_");
  public static final HistoricCaseActivityInstanceQueryProperty END = new HistoricCaseActivityInstanceQueryProperty("END_TIME_");
  public static final HistoricCaseActivityInstanceQueryProperty DURATION = new HistoricCaseActivityInstanceQueryProperty("DURATION_");

  private String name;

  public HistoricCaseActivityInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static HistoricCaseActivityInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
