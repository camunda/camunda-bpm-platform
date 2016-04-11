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

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.query.QueryProperty;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * <p>A QueryOrderingProperty specifies a condition by which the results of a query should be
 * sorted. It can either specify a sorting by a property of the entities to be selected or
 * a sorting by a property of a related entity. For example in a {@link TaskQuery},
 * the entity to be selected is {@link Task} while a related entity could be a
 * {@link VariableInstance}.</p>
 *
 * <p>It is made up of the following:</p>
 *
 * <p>
 * <dl>
 *   <dt>relation</dt>
 *     <dd>A symbolic name that identifies a related entity. <code>null</code> if
 *     an ordering over a property of the entity to be selected is expressed.</dd>
 *   <dt>queryProperty</dt>
 *     <dd>The property to be sorted on. An instance of {@link QueryProperty}.</dd>
 *   <dt>direction</dt>
 *     <dd>The ordering direction, refer to {@link Direction}</dd>
 *   <dt>relationConditions</dt>
 *     <dd>A list of constraints that describe the nature of the relation to another entity
 *     (or in SQL terms, the joining conditions). Is <code>null</code> if relation
 *     is <code>null</code>. Contains instances of {@link QueryEntityRelationCondition}.</dd>
 * <dl>
 * </p>
 *
 * @author Thorben Lindhauer
 */
public class QueryOrderingProperty implements Serializable {

  public static final String RELATION_VARIABLE = "variable";
  public static final String RELATION_PROCESS_DEFINITION = "process-definition";
  public static final String RELATION_CASE_DEFINITION = "case-definition";

  protected static final long serialVersionUID = 1L;

  protected String relation;
  protected QueryProperty queryProperty;
  protected Direction direction;
  protected List<QueryEntityRelationCondition> relationConditions;

  public QueryOrderingProperty() {
  }

  public QueryOrderingProperty(QueryProperty queryProperty, Direction direction) {
    this.queryProperty = queryProperty;
    this.direction = direction;
  }

  public QueryOrderingProperty(String relation, QueryProperty queryProperty) {
    this.relation = relation;
    this.queryProperty = queryProperty;
  }

  public QueryProperty getQueryProperty() {
    return queryProperty;
  }

  public void setQueryProperty(QueryProperty queryProperty) {
    this.queryProperty = queryProperty;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public Direction getDirection() {
    return direction;
  }

  public List<QueryEntityRelationCondition> getRelationConditions() {
    return relationConditions;
  }

  public void setRelationConditions(List<QueryEntityRelationCondition> relationConditions) {
    this.relationConditions = relationConditions;
  }

  public boolean hasRelationConditions() {
    return relationConditions != null && !relationConditions.isEmpty();
  }

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  /**
   * @return whether this ordering property is contained in the default fields
   * of the base entity (e.g. task.NAME_ is a contained property; LOWER(task.NAME_) or
   * variable.TEXT_ (given a task query) is not contained)
   */
  public boolean isContainedProperty() {
    return relation == null && queryProperty.getFunction() == null;
  }

  @Override
  public String toString() {

    return "QueryOrderingProperty["
      + "relation=" + relation
      + ", queryProperty=" + queryProperty
      + ", direction=" + direction
      + ", relationConditions=" + getRelationConditionsString()
      + "]";
  }

  public String getRelationConditionsString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    if(relationConditions != null) {
      for (int i = 0; i < relationConditions.size(); i++) {
        if (i > 0) {
          builder.append(",");
        }
        builder.append(relationConditions.get(i));
      }
    }
    builder.append("]");
    return builder.toString();
  }

}
