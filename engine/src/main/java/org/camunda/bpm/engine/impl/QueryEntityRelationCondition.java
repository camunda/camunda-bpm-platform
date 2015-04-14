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
 * Specifies a condition by which two entity types can be related.
 * <code>comparisonProperty</code> and <code>scalarValue</code>
 * are exclusive, i.e. one of the should be <code>null</code>.
 *
 * @author Thorben Lindhauer
 */
public class QueryEntityRelationCondition {

  protected QueryProperty property;
  protected QueryProperty comparisonProperty;
  protected Object scalarValue;

  public QueryEntityRelationCondition(QueryProperty queryProperty, Object scalarValue) {
    this(queryProperty, null, scalarValue);
  }

  public QueryEntityRelationCondition(QueryProperty queryProperty, QueryProperty comparisonProperty) {
    this(queryProperty, comparisonProperty, null);
  }

  public QueryEntityRelationCondition(QueryProperty queryProperty, QueryProperty comparisonProperty,
      Object scalarValue) {
    this.property = queryProperty;
    this.comparisonProperty = comparisonProperty;
    this.scalarValue = scalarValue;
  }

  public QueryProperty getProperty() {
    return property;
  }

  public QueryProperty getComparisonProperty() {
    return comparisonProperty;
  }

  public Object getScalarValue() {
    return scalarValue;
  }

  /**
   * This assumes that scalarValue and comparisonProperty are mutually exclusive.
   * Either a condition is expressed is by a scalar value, or with a property of another entity.
   */
  public boolean isPropertyComparison() {
    return comparisonProperty != null;
  }

  public String toString() {
    return "QueryEntityRelationCondition["
      + "property=" + property
      + ", comparisonProperty=" + comparisonProperty
      + ", scalarValue=" + scalarValue
      + "]";
  }
}
