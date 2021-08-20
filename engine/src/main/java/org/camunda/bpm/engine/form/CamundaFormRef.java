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

/**
 * A {@link CamundaFormRef} represents a reference to a deployed Camunda Form.
 */
public interface CamundaFormRef {

  /**
   * The key of a {@link CamundaFormRef} corresponds to the {@code id} attribute
   * in the Camunda Forms JSON.
   */
  String getKey();

  /**
   * The binding of {@link CamundaFormRef} specifies which version of the form
   * to reference. Possible values are: {@code latest}, {@code deployment} and
   * {@code version} (specific version value can be retrieved with {@link #getVersion()}).
   */
  String getBinding();

  /**
   * If the {@link #getBinding() binding} of a {@link CamundaFormRef} is set to
   * {@code version}, the specific version is returned.
   */
  Integer getVersion();
}
