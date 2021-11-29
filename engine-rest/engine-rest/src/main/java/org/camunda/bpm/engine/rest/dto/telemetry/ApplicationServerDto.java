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

import static org.camunda.bpm.engine.impl.util.ParseUtil.parseServerVendor;

import org.camunda.bpm.engine.telemetry.ApplicationServer;

public class ApplicationServerDto {

  protected String vendor;
  protected String version;

  public ApplicationServerDto(String vendor, String version) {
    this.vendor = vendor;
    this.version = version;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public static ApplicationServerDto fromEngineDto(ApplicationServer other) {
    return new ApplicationServerDto(
        other.getVendor(),
        other.getVersion());
  }
}
