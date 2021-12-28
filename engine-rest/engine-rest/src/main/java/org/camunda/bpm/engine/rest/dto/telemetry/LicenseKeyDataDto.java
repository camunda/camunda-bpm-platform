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

import java.util.Map;

import org.camunda.bpm.engine.telemetry.LicenseKeyData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LicenseKeyDataDto {

  public static final String SERIALIZED_VALID_UNTIL = "valid-until";
  public static final String SERIALIZED_IS_UNLIMITED = "unlimited";

  protected String customer;
  protected String type;
  @JsonProperty(value = SERIALIZED_VALID_UNTIL)
  protected String validUntil;
  @JsonProperty(value = SERIALIZED_IS_UNLIMITED)
  protected Boolean isUnlimited;
  protected Map<String, String> features;
  protected String raw;

  public LicenseKeyDataDto(String customer, String type, String validUntil, Boolean isUnlimited, Map<String, String> features, String raw) {
    this.customer = customer;
    this.type = type;
    this.validUntil = validUntil;
    this.isUnlimited = isUnlimited;
    this.features = features;
    this.raw = raw;
  }

  public String getCustomer() {
    return customer;
  }

  public void setCustomer(String customer) {
    this.customer = customer;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(String validUntil) {
    this.validUntil = validUntil;
  }

  public Boolean isUnlimited() {
    return isUnlimited;
  }

  public void setUnlimited(Boolean isUnlimited) {
    this.isUnlimited = isUnlimited;
  }

  public Map<String, String> getFeatures() {
    return features;
  }

  public void setFeatures(Map<String, String> features) {
    this.features = features;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public static LicenseKeyDataDto fromEngineDto(LicenseKeyData other) {
    return new LicenseKeyDataDto(
        other.getCustomer(),
        other.getType(),
        other.getValidUntil(),
        other.isUnlimited(),
        other.getFeatures(),
        other.getRaw());
  }
}
