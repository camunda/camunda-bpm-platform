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
package org.camunda.spin.plugin.impl.feel.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.feel.impl.spi.CustomValueMapper;
import org.camunda.feel.impl.spi.SpiServiceLoader;
import org.camunda.feel.interpreter.impl.ValueMapper;
import org.junit.Test;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;

public class FeelSpiValueMapperTest {

  @Test
  public void shouldProvideSpinValueMapperInCompositeMapper() {
    // given
    ValueMapper.CompositeValueMapper compositeMapper = (ValueMapper.CompositeValueMapper) SpiServiceLoader.loadValueMapper();

    // when
    List<CustomValueMapper> customValueMapperList = compositeMapper.customMappers();
    java.util.List<CustomValueMapper> mappersList = JavaConverters.asJava(customValueMapperList);
    long count = mappersList.stream().filter(customValueMapper -> CamundaSpinValueMapper.class.equals(customValueMapper.getClass())).count();

    // then
    assertThat(count).isOne();
  }
}