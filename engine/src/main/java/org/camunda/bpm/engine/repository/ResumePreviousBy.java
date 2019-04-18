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
package org.camunda.bpm.engine.repository;


/**
 * Contains the constants for the possible values the property {@link ProcessApplicationDeploymentBuilder#resumePreviousVersionsBy(String)}.
 */
public enum ResumePreviousBy {
  ;

  /**
   * Resume previous deployments that contain processes with the same key as in the new deployment
   */
  public static final String RESUME_BY_PROCESS_DEFINITION_KEY = "process-definition-key";

  /**
   * Resume previous deployments that have the same name as the new deployment
   */
  public static final String RESUME_BY_DEPLOYMENT_NAME = "deployment-name";
}