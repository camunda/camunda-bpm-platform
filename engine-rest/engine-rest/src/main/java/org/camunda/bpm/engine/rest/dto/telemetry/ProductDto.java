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
package org.camunda.bpm.engine.rest.dto.telemetry;

import org.camunda.bpm.engine.telemetry.Product;

public class ProductDto {

  protected String name;
  protected String version;
  protected String edition;
  protected InternalsDto internals;

  public ProductDto(String name, String version, String edition, InternalsDto internals) {
    this.name = name;
    this.version = version;
    this.edition = edition;
    this.internals = internals;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public InternalsDto getInternals() {
    return internals;
  }

  public void setInternals(InternalsDto internals) {
    this.internals = internals;
  }

  public static ProductDto fromEngineDto(Product other) {
    return new ProductDto(
        other.getName(),
        other.getVersion(),
        other.getEdition(),
        InternalsDto.fromEngineDto(other.getInternals()));
  }

}
