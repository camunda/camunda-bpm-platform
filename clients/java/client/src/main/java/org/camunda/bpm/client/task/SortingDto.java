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

package org.camunda.bpm.client.task;

import org.camunda.bpm.client.task.OrderingConfig.OrderingProperty;

/**
 * DTO that encapsulates the sorting parameters used for making requests against the fetch and lock API.
 */
public class SortingDto {

  protected String sortBy;
  protected String sortOrder;

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * Static factory method to create a {@link SortingDto} from a field and its order.
   *
   * @param sortBy    the string representation of the given field
   * @param sortOrder the string representation of the given order to use for the associated field
   * @return the result {@link SortingDto}
   */
  public static SortingDto of(String sortBy, String sortOrder) {
    SortingDto result = new SortingDto();
    result.setSortBy(sortBy);
    result.setSortOrder(sortOrder);

    return result;
  }

  /**
   * Static factory method to create a {@link SortingDto} from a given {@link OrderingProperty}.
   */
  protected static SortingDto fromOrderingProperty(OrderingProperty property) {
    String sortBy = property.getField().getName();
    String sortOrder = property.getDirection() != null ? property.getDirection().asString() : null;

    return SortingDto.of(sortBy, sortOrder);
  }

}
