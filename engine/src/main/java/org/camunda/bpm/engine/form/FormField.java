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
package org.camunda.bpm.engine.form;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * <p>Represents an individual field in a form.</p>
 *
 * @author Michael Siebers
 * @author Daniel Meyer
 *
 */
public interface FormField {

  /**
   * @return the Id of a form property. Must be unique for a given form.
   * The id is used for mapping the form field to a process variable.
   */
  public String getId();

  /**
   * @return the human-readable display name of a form property.
   */
  public String getLabel();

  /**
   * @return the type of this form field.
   */
  public FormType getType();

  /**
   * @return the name of the type of this form field
   */
  public String getTypeName();

  /**
   * @return the default value for this form field.
   */
  @Deprecated
  public Object getDefaultValue();

  /**
   * @return the value for this form field
   */
  public TypedValue getValue();

  /**
   * @return a list of {@link FormFieldValidationConstraint ValidationConstraints}.
   */
  public List<FormFieldValidationConstraint> getValidationConstraints();

  /**
   * @return a {@link Map} of additional properties. This map may be used for adding additional configuration
   * to a form field. An example may be layout hints such as the size of the rendered form field or information
   * about an icon to prepend or append to the rendered form field.
   */
  public Map<String, String> getProperties();

  /**
   * @return true if field is defined as businessKey, false otherwise
   */
  boolean isBusinessKey();
  
}
