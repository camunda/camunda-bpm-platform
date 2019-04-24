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
package org.camunda.bpm.engine.rest.sub;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.impl.VariableResponseProvider;
import org.camunda.bpm.engine.variable.value.TypedValue;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Base class to unify the getResource(boolean deserialized) and
 * getResourceBinary() methods for several subclasses. (formerly getVariable()
 * and getBinaryVariable())
 *
 * @author Ronny Bräunlich
 *
 */
public abstract class AbstractResourceProvider<T extends Query<?, U>, U, DTO> {

  protected String id;
  protected ProcessEngine engine;

  public AbstractResourceProvider(String detailId, ProcessEngine engine) {
    this.id = detailId;
    this.engine = engine;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public DTO getResource(@QueryParam(VariableResource.DESERIALIZE_VALUE_QUERY_PARAM) @DefaultValue("true") boolean deserializeObjectValue) {
    U variableInstance = baseQueryForVariable(deserializeObjectValue).singleResult();
    if (variableInstance != null) {
      return transformToDto(variableInstance);
    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, getResourceNameForErrorMessage() + " with Id '" + id + "' does not exist.");
    }
  }


  @GET
  @Path("/data")
  public Response getResourceBinary() {
    U queryResult = baseQueryForBinaryVariable().singleResult();
    if (queryResult != null) {
      TypedValue variableInstance = transformQueryResultIntoTypedValue(queryResult);
      return new VariableResponseProvider().getResponseForTypedVariable(variableInstance, id);
    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, getResourceNameForErrorMessage() + " with Id '" + id + "' does not exist.");
    }
  }


  protected String getId() {
    return id;
  }

  protected ProcessEngine getEngine() {
    return engine;
  }

  /**
   * Create the query we need for fetching the desired result. Setting
   * properties in the query like disableCustomObjectDeserialization() or
   * disableBinaryFetching() should be done in this method.
   */
  protected abstract Query<T, U> baseQueryForBinaryVariable();

  /**
   * TODO change comment Create the query we need for fetching the desired
   * result. Setting properties in the query like
   * disableCustomObjectDeserialization() or disableBinaryFetching() should be
   * done in this method.
   *
   * @param deserializeObjectValue
   */
  protected abstract Query<T, U> baseQueryForVariable(boolean deserializeObjectValue);

  protected abstract TypedValue transformQueryResultIntoTypedValue(U queryResult);

  protected abstract DTO transformToDto(U queryResult);

  protected abstract String getResourceNameForErrorMessage();

}
