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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;

/**
 * Class that encapsulates the client's configuration of createTime ordering.
 */
public class OrderingConfig {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected final List<OrderingProperty> orderingProperties;

  /**
   * Constructor using a list of ordering properties
   *
   * @param orderingProperties the list of ordering properties
   */
  protected OrderingConfig(List<OrderingProperty> orderingProperties) {
    this.orderingProperties = orderingProperties;
  }

  /**
   * Returns an empty config.
   */
  public static OrderingConfig empty() {
    return new OrderingConfig(new ArrayList<>());
  }

  /**
   * Configures the given field.
   *
   * @param field the sorting field to configure
   */
  public void configureField(SortingField field) {
    orderingProperties.add(OrderingProperty.of(field, null));
  }

  /**
   * Configures the {@link Direction} for the last configured field on this {@link OrderingConfig}.
   *
   * @param direction the given direction, nullable.
   */
  public void configureDirectionOnLastField(Direction direction) {
    OrderingProperty lastConfiguredProperty = validateAndGetLastConfiguredProperty();

    if (lastConfiguredProperty.getDirection() != null) {
      throw LOG.doubleDirectionConfigException();
    }

    lastConfiguredProperty.setDirection(direction);
    orderingProperties.add(lastConfiguredProperty);
  }

  /**
   * Validates the last configured field for its direction and retrieves it.
   */
  protected OrderingProperty validateAndGetLastConfiguredProperty() {
    OrderingProperty lastConfiguredProperty = getLastConfiguredProperty();

    if (lastConfiguredProperty == null) {
      throw LOG.unspecifiedOrderByMethodException();
    }

    return lastConfiguredProperty;
  }

  /**
   * Validates ordering properties all have a non-null direction.
   */
  public void validateOrderingProperties() {
    boolean hasMissingDirection = orderingProperties.stream()
        .anyMatch(p -> p.getDirection() == null);

    if (hasMissingDirection) {
      throw LOG.missingDirectionException();
    }
  }

  /**
   * Converts this {@link OrderingConfig} to a list of {@link SortingDto}s.
   */
  public List<SortingDto> toSortingDtos() {
    return orderingProperties.stream()
        .map(SortingDto::fromOrderingProperty)
        .collect(Collectors.toList());
  }

  /**
   * Returns the last configured field in this {@link OrderingConfig}.
   */
  protected OrderingProperty getLastConfiguredProperty() {
    return !orderingProperties.isEmpty() ? orderingProperties.get(orderingProperties.size() - 1) : null;
  }

  /**
   * The field to sort by.
   */
  public enum SortingField {

    CREATE_TIME("createTime");

    private final String name;

    SortingField(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }

  /**
   * The direction of createTime.
   */
  public enum Direction {
    ASC, DESC;

    public String asString() {
      return super.name().toLowerCase();
    }
  }

  /**
   * Static Class that encapsulates an ordering property with a field and its direction.
   */
  public static class OrderingProperty {

    protected SortingField field;
    protected Direction direction;

    /**
     * Static factory method to create {@link OrderingProperty} out of a field and its corresponding {@link Direction}.
     */
    public static OrderingProperty of(SortingField field, Direction direction) {
      OrderingProperty result = new OrderingProperty();
      result.setField(field);
      result.setDirection(direction);

      return result;
    }

    public void setField(SortingField field) {
      this.field = field;
    }

    public SortingField getField() {
      return this.field;
    }

    public void setDirection(Direction direction) {
      this.direction = direction;
    }

    public Direction getDirection() {
      return this.direction;
    }
  }

}