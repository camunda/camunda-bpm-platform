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

/** An object structure representing a Camunda Form used to present forms to users
 * either when starting a process instance or when assigned to a User Task.
 *
 * Camunda Forms are usually composed with the help of tools like the Camunda Modeler
 * and deployed to the engine along with or separate from other resources, like process
 * definitions.
 *
 * On execution of the process instance a referenced Camunda Form (either through Start
 * Events or User Tasks) is represented by an instance of this class.
 */
public interface CamundaFormDefinition extends ResourceDefinition {

}
