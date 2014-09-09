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

package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.json.JsonTaskQueryConverter;
import org.camunda.bpm.engine.impl.util.json.JSONException;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public class FilterEntity implements Filter, Serializable, DbEntity {

  private static final long serialVersionUID = 1L;

  public final static Map<String, JsonObjectConverter> queryConverter = new HashMap<String, JsonObjectConverter>();

  static {
    queryConverter.put(EntityTypes.TASK, new JsonTaskQueryConverter());
  }

  protected String id;
  protected String resourceType;
  protected String name;
  protected String owner;
  protected String query;
  protected String properties;

  public FilterEntity() {
    setQuery("{}");
  }

  public FilterEntity(String resourceType) {
    this();
    setResourceType(resourceType);
  }

  public FilterEntity(String resourceType, String name) {
    this(resourceType);
    setName(name);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getResourceType() {
    return resourceType;
  }

  public Filter setResourceType(String resourceType) {
    ensureNotEmpty("Filter resource type must not be null or empty", "resourceType", resourceType);
    this.resourceType = resourceType;
    return this;
  }

  public String getName() {
    return name;
  }

  public Filter setName(String name) {
    ensureNotEmpty("Filter name must not be null or empty", "name", name);
    this.name = name;
    return this;
  }

  public String getOwner() {
    return owner;
  }

  public Filter setOwner(String owner) {
    this.owner = owner;
    return this;
  }

  public String getQuery() {
    return query;
  }

  @SuppressWarnings("unchecked")
  public <T extends Query> T getTypeQuery() {
    JsonObjectConverter converter = getConverter();
    return (T) converter.toObject(query);
  }

  public Filter setQuery(String query) {
    ensureNotEmpty("Query must not be null or empty. Has to be a JSON object", "query", query);
    try {
      new JSONObject(query);
    }
    catch (JSONException e) {
      throw new ProcessEngineException("Query string has to be a JSON object", e);
    }
    this.query = query;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T extends Query> Filter setQuery(T query) {
    ensureNotNull("query", query);
    JsonObjectConverter converter = getConverter();
    setQuery(converter.toJson(query));
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T extends Query> Filter extend(T extendingQuery) {
    ensureNotNull("extendingQuery", extendingQuery);

    // parse query to JSON
    JSONObject queryJson = new JSONObject(query);

    // convert extendingQuery to JSON
    JsonObjectConverter converter = getConverter();
    JSONObject extendingQueryJson = converter.toJsonObject(extendingQuery);

    // merge queries by keys
    Iterator<String> extendingKeys = extendingQueryJson.keys();
    while (extendingKeys.hasNext()) {
      String key = extendingKeys.next();
      queryJson.put(key, extendingQueryJson.get(key));
    }

    // save merged query
    setQuery(queryJson.toString());

    return this;
  }

  public String getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getPropertiesMap() {
    if (properties != null) {
      Map<String, Object> map = new HashMap<String, Object>();
      JSONObject json = new JSONObject(properties);
      Iterator<String> keys = json.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        if (json.optJSONObject(key) != null) {
          map.put(key, json.getJSONObject(key).toString());
        }
        else if (json.optJSONArray(key) != null) {
          map.put(key, json.getJSONArray(key).toString());
        }
        else {
          map.put(key, json.get(key));
        }
      }
      return map;
    }
    else {
      return null;
    }
  }

  public Filter setProperties(String properties) {
    if (properties != null && properties.isEmpty()) {
      properties = null;
    }

    this.properties = properties;
    return this;
  }

  public Filter setProperties(Map<String, Object> properties) {
    if (properties != null) {
      JSONObject json = new JSONObject(properties);
      setProperties(json.toString());
    }
    else {
      this.properties = null;
    }
    return this;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", this.name);
    persistentState.put("owner", this.owner);
    persistentState.put("query", this.query);
    persistentState.put("properties", this.properties);
    return persistentState;
  }

  protected JsonObjectConverter getConverter() {
    JsonObjectConverter converter = queryConverter.get(resourceType);
    if (converter != null) {
      return converter;
    }
    else {
      throw new ProcessEngineException("Unsupported resource type '" + resourceType + "'");
    }
  }

}
