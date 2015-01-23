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

import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.query.QueryProperty;



/**
 * Contains the possible properties that can be used by the {@link GroupQuery}.
 *
 * @author Joram Barrez
 */
public interface GroupQueryProperty {

  public static final QueryProperty GROUP_ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty NAME = new QueryPropertyImpl("NAME_");
  public static final QueryProperty TYPE = new QueryPropertyImpl("TYPE_");
}
