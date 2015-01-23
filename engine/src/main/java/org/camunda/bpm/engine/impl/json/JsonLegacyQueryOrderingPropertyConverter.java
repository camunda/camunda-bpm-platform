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
package org.camunda.bpm.engine.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * Deserializes query ordering properties from the deprecated 7.2 format in which
 * the SQL-like orderBy parameter was used.
 *
 * Is able to deserialize strings like:
 *
 * <ul>
 *   <li>RES.ID_ asc</li>
 *   <li>LOWER(RES.NAME_) desc</li>
 *   <li>RES.ID_ asc, RES.NAME_ desc</li>
 * </ul>
 *
 * @author Thorben Lindhauer
 */
public class JsonLegacyQueryOrderingPropertyConverter {

  public static final String ORDER_BY_DELIMITER = ",";

  public static JsonLegacyQueryOrderingPropertyConverter INSTANCE =
      new JsonLegacyQueryOrderingPropertyConverter();

  public List<QueryOrderingProperty> fromOrderByString(String orderByString) {
    List<QueryOrderingProperty> properties = new ArrayList<QueryOrderingProperty>();

    String[] orderByClauses = orderByString.split(ORDER_BY_DELIMITER);

    for (String orderByClause : orderByClauses) {
      orderByClause = orderByClause.trim();
      String[] clauseParts = orderByClause.split(" ");

      if (clauseParts.length == 0) {
        continue;
      } else if (clauseParts.length > 2) {
        throw new ProcessEngineException("Invalid order by clause: " + orderByClause);
      }

      String function = null;

      String propertyPart = clauseParts[0];

      int functionArgumentBegin = propertyPart.indexOf("(");
      if (functionArgumentBegin >= 0) {
        function = propertyPart.substring(0, functionArgumentBegin);
        int functionArgumentEnd = propertyPart.indexOf(")");

        propertyPart = propertyPart.substring(functionArgumentBegin + 1, functionArgumentEnd);
      }

      String[] propertyParts = propertyPart.split("\\.");

      String property = null;
      if (propertyParts.length == 1) {
        property = propertyParts[0];
      } else if (propertyParts.length == 2) {
        property = propertyParts[1];
      } else {
        throw new ProcessEngineException("Invalid order by property part: " + clauseParts[0]);
      }

      QueryProperty queryProperty = new QueryPropertyImpl(property, function);

      Direction direction = null;
      if (clauseParts.length == 2) {
        String directionPart = clauseParts[1];
        direction = Direction.findByName(directionPart);
      }

      QueryOrderingProperty orderingProperty = new QueryOrderingProperty(null, queryProperty);
      orderingProperty.setDirection(direction);
      properties.add(orderingProperty);
    }

    return properties;
  }

}
