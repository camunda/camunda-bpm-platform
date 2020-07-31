/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.QueryValidators.StoredQueryValidator;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.json.JsonTaskQueryConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public class FilterEntity implements Filter, Serializable, DbEntity, HasDbRevision, HasDbReferences, DbEntityLifecycleAware {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public final static Map<String, JsonObjectConverter<?>> queryConverter = new HashMap<String, JsonObjectConverter<?>>();

  static {
    queryConverter.put(EntityTypes.TASK, new JsonTaskQueryConverter());
  }

  protected String id;
  protected String resourceType;
  protected String name;
  protected String owner;
  protected AbstractQuery query;
  protected Map<String, Object> properties;
  protected int revision = 0;

  protected FilterEntity() {
  }

  public FilterEntity(String resourceType) {
    setResourceType(resourceType);
    setQueryInternal("{}");
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Filter setResourceType(String resourceType) {
    ensureNotEmpty(NotValidException.class, "Filter resource type must not be null or empty", "resourceType", resourceType);
    ensureNull(NotValidException.class, "Cannot overwrite filter resource type", "resourceType", this.resourceType);

    this.resourceType = resourceType;
    return this;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getName() {
    return name;
  }

  public Filter setName(String name) {
    ensureNotEmpty(NotValidException.class, "Filter name must not be null or empty", "name", name);
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

  @SuppressWarnings("unchecked")
  public <T extends Query<?, ?>> T getQuery() {
    return (T) query;
  }

  public String getQueryInternal() {
    JsonObjectConverter<Object> converter = getConverter();
    return converter.toJson(query);
  }

  public <T extends Query<?, ?>> Filter setQuery(T query) {
    ensureNotNull(NotValidException.class, "query", query);
    this.query = (AbstractQuery<?, ?>) query;
    return this;
  }

  public void setQueryInternal(String query) {
    ensureNotNull(NotValidException.class, "query", query);
    JsonObjectConverter<Object> converter = getConverter();
    this.query = (AbstractQuery<?, ?>) converter.toObject(JsonUtil.asObject(query));
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public String getPropertiesInternal() {
    return JsonUtil.asString(properties);
  }

  public Filter setProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public void setPropertiesInternal(String properties) {
    if (properties != null) {
      JsonObject json = JsonUtil.asObject(properties);
      this.properties = JsonUtil.asMap(json);
    }
    else {
      this.properties = null;
    }
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  @SuppressWarnings("unchecked")
  public <T extends Query<?, ?>> Filter extend(T extendingQuery) {
    ensureNotNull(NotValidException.class, "extendingQuery", extendingQuery);

    if (!extendingQuery.getClass().equals(query.getClass())) {
      throw LOG.queryExtensionException(query.getClass().getName(), extendingQuery.getClass().getName());
    }

    FilterEntity copy = copyFilter();
    copy.setQuery(query.extend(extendingQuery));

    return copy;
  }

  @SuppressWarnings("unchecked")
  protected <T> JsonObjectConverter<T> getConverter() {
    JsonObjectConverter<T> converter = (JsonObjectConverter<T>) queryConverter.get(resourceType);
    if (converter != null) {
      return converter;
    }
    else {
      throw LOG.unsupportedResourceTypeException(resourceType);
    }
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", this.name);
    persistentState.put("owner", this.owner);
    persistentState.put("query", this.query);
    persistentState.put("properties", this.properties);
    return persistentState;
  }

  protected FilterEntity copyFilter() {
    FilterEntity copy = new FilterEntity(getResourceType());
    copy.setName(getName());
    copy.setOwner(getOwner());
    copy.setQueryInternal(getQueryInternal());
    copy.setPropertiesInternal(getPropertiesInternal());
    return copy;
  }

  public void postLoad() {
    if (query != null) {
      query.addValidator(StoredQueryValidator.get());
    }

  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<String>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<String, Class>();
    return referenceIdAndClass;
  }
}
