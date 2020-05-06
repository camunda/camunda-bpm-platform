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
package org.camunda.bpm.engine.impl.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.util.ImmutablePair;
import org.junit.Test;

public class BatchElementConfigurationTest {

  @Test
  public void shouldProduceListOfIdsSortedByKey() {
    // given
    BatchElementConfiguration configuration = new BatchElementConfiguration();
    configuration.addDeploymentMappings(Arrays.asList(
        new ImmutablePair<>("ABC", "foo"), new ImmutablePair<>("ABC", "bar"), new ImmutablePair<>("AAB", "baz")));
    // when
    List<String> ids = configuration.getIds();
    // then
    assertThat(ids).containsExactlyInAnyOrder("foo", "bar", "baz");
    assertThat(ids.get(0)).isEqualTo("baz");
  }

  @Test
  public void shouldProduceListOfMappingsSortedByKey() {
    // given
    BatchElementConfiguration configuration = new BatchElementConfiguration();
    configuration.addDeploymentMappings(Arrays.asList(
        new ImmutablePair<>("ABC", "foo"), new ImmutablePair<>("ABC", "bar"), new ImmutablePair<>("AAB", "baz")));
    // when
    DeploymentMappings mappings = configuration.getMappings();
    // then
    assertThat(mappings).containsExactly(new DeploymentMapping("AAB", 1), new DeploymentMapping("ABC", 2));
  }

  @Test
  public void shouldIncludeNullMappings() {
    // given
    BatchElementConfiguration configuration = new BatchElementConfiguration();
    configuration.addDeploymentMappings(Arrays.asList(
        new ImmutablePair<>("ABC", "foo"), new ImmutablePair<>("AAB", "baz"), new ImmutablePair<>(null, "null")));
    // when
    List<String> ids = configuration.getIds();
    DeploymentMappings mappings = configuration.getMappings();
    // then
    assertThat(ids).containsExactly("baz", "foo", "null");
    assertThat(mappings).containsExactly(new DeploymentMapping("AAB", 1), new DeploymentMapping("ABC", 1), new DeploymentMapping(null, 1));
  }

  @Test
  public void shouldRecalculateMappingsWhenNewElementsAdded() {
    // given
    BatchElementConfiguration configuration = new BatchElementConfiguration();
    configuration.addDeploymentMappings(Arrays.asList(
        new ImmutablePair<>("ABC", "foo"), new ImmutablePair<>("AAB", "baz")));
    configuration.getIds();
    configuration.getMappings();
    // when
    configuration.addDeploymentMappings(Arrays.asList(
        new ImmutablePair<>("AAB", "bar")));
    List<String> ids = configuration.getIds();
    DeploymentMappings mappings = configuration.getMappings();
    // then
    assertThat(ids).containsExactlyInAnyOrder("baz", "foo", "bar");
    assertThat(ids.get(ids.size() - 1)).isEqualTo("foo");
    assertThat(mappings).containsExactly(new DeploymentMapping("AAB", 2), new DeploymentMapping("ABC", 1));
  }

  @Test
  public void shouldIncludeNullMappingForUnmappedIds() {
    // given
    BatchElementConfiguration configuration = new BatchElementConfiguration();
    configuration.addDeploymentMappings(
        Arrays.asList(new ImmutablePair<>("ABC", "foo"), new ImmutablePair<>("AAB", "baz")),
        Arrays.asList("null"));
    // when
    List<String> ids = configuration.getIds();
    DeploymentMappings mappings = configuration.getMappings();
    // then
    assertThat(ids).containsExactly("baz", "foo", "null");
    assertThat(mappings).containsExactly(new DeploymentMapping("AAB", 1), new DeploymentMapping("ABC", 1), new DeploymentMapping(null, 1));
  }

  @Test
  public void shouldAddToNullMappingsForUnmappedIds() {
    // given
    BatchElementConfiguration configuration = new BatchElementConfiguration();
    configuration.addDeploymentMappings(
        Arrays.asList(new ImmutablePair<>("ABC", "foo"), new ImmutablePair<>("AAB", "baz"), new ImmutablePair<>(null, "null")),
        Arrays.asList("bar"));
    // when
    List<String> ids = configuration.getIds();
    DeploymentMappings mappings = configuration.getMappings();
    // then
    assertThat(ids).startsWith("baz", "foo").containsExactlyInAnyOrder("baz", "foo", "bar", "null");
    assertThat(mappings).containsExactly(new DeploymentMapping("AAB", 1), new DeploymentMapping("ABC", 1), new DeploymentMapping(null, 2));
  }
}
