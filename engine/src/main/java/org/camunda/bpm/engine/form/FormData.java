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


/** Contains all metadata for displaying a form and serves as
 * base interface for {@link StartFormData} and {@link TaskFormData}
 *
 * @author Tom Baeyens
 * @author Michael Siebers
 * @author Daniel Meyer
 *
 */
public interface FormData {

  /** User-defined reference to a form. In the Camunda Tasklist application,
   * it is assumed that the form key specifies a resource in the deployment
   * which is the template for the form.  But users are free to
   * use this property differently.
   *
   * A form can be referenced either through a form key or through a {@link CamundaFormRef}.
   *
   */
  String getFormKey();

  /**
   * User-defined reference to a form. A {@link CamundaFormRef} can specify any
   * Camunda Form deployed to the engine with any deployment. It is also possible
   * to specify a specific version of a deployed form.
   *
   * A form can be referenced either through a form key or through a {@link CamundaFormRef}.
   *
   */
  CamundaFormRef getCamundaFormRef();

  /** The deployment id of the process definition to which this form is related
   *  */
  String getDeploymentId();

  /** Properties containing the dynamic information that needs to be displayed in the form. */
  @Deprecated
  List<FormProperty> getFormProperties();

  /** returns the form fields which make up this form. */
  List<FormField> getFormFields();

}
