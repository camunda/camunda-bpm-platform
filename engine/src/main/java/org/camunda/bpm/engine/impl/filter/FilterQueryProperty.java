/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.filter;

import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Sebastian Menski
 */
public interface FilterQueryProperty {

  public static final QueryProperty FILTER_ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty RESOURCE_TYPE = new QueryPropertyImpl("RESOURCE_TYPE_");
  public static final QueryProperty NAME = new QueryPropertyImpl("NAME_");
  public static final QueryProperty OWNER = new QueryPropertyImpl("OWNER_");
  public static final QueryProperty QUERY = new QueryPropertyImpl("QUERY_");
  public static final QueryProperty PROPERTIES = new QueryPropertyImpl("PROPERTIES_");

}
