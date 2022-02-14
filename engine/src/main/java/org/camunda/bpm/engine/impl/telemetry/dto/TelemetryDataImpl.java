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
package org.camunda.bpm.engine.impl.telemetry.dto;

import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.telemetry.TelemetryData;

public class TelemetryDataImpl implements TelemetryData {

  protected String installation;
  protected ProductImpl product;

  public TelemetryDataImpl(String installation, ProductImpl product) {
    this.installation = installation;
    this.product = product;
  }

  public TelemetryDataImpl(TelemetryDataImpl other) {
    this(other.installation, new ProductImpl(other.product));
  }

  public String getInstallation() {
    return installation;
  }

  public void setInstallation(String installation) {
    this.installation = installation;
  }

  public ProductImpl getProduct() {
    return product;
  }

  public void setProduct(ProductImpl product) {
    this.product = product;
  }

  public void mergeInternals(InternalsImpl other) {
    product.getInternals().mergeDynamicData(other);
  }

  @Override
  public String toString() {
    return JsonUtil.asString(this);
  }
}
