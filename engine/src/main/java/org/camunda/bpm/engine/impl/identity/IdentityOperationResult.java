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
package org.camunda.bpm.engine.impl.identity;

import java.io.Serializable;

import org.camunda.bpm.engine.IdentityService;

/**
 * <p>
 * Holds the result of an {@link IdentityService} operation including the
 * operation that was executed and optionally a resulting value.
 * </p>
 * 
 * <p>
 * This metadata can be used to distinguish between the different resulting
 * actions of a logical operation, e.g. if the 'save' operation lead to an
 * 'update' or an 'insert'.
 * </p>
 * 
 * @author Tobias Metzke
 *
 */
public class IdentityOperationResult {
  
  public static final String OPERATION_CREATE = "create";
  public static final String OPERATION_UPDATE = "update";
  public static final String OPERATION_DELETE = "delete";
  public static final String OPERATION_UNLOCK = "unlock";
  public static final String OPERATION_NONE = "none";

  protected Serializable value;
  protected String operation;
  
  public IdentityOperationResult(Serializable value, String operation) {
    this.value = value;
    this.operation = operation;
  }

  public Serializable getValue() {
    return value;
  }

  public void setValue(Serializable value) {
    this.value = value;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }
}
