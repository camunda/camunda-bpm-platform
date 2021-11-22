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
package org.camunda.bpm.engine.telemetry;

/**
 * This class represents the data structure of information about the Camunda
 * product.
 *
 * This information is sent to Camunda when telemetry is enabled.
 *
 * @see <a href=
 *      "https://docs.camunda.org/manual/latest/introduction/telemetry/#collected-data">Camunda
 *      Documentation: Collected Telemetry Data</a>
 */
public interface Product {

  /**
   * The name of the product (i.e., Camunda BPM Runtime).
   */
  public String getName();

  /**
   * The version of the process engine (i.e., 7.X.Y).
   */
  public String getVersion();

  /**
   * The edition of the product (i.e., either community or enterprise).
   */
  public String getEdition();

  /**
   * Information about the technical internals and the environment of the
   * Camunda installation.
   */
  public Internals getInternals();
}
