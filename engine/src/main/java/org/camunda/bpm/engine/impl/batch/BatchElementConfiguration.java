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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.camunda.bpm.engine.impl.util.ImmutablePair;

/**
 * Configuration information on the elements of a batch
 */
public class BatchElementConfiguration {
  protected static final Comparator<String> NULLS_LAST_STRING_COMPARATOR = Comparator.nullsLast(String::compareToIgnoreCase);

  protected SortedMap<String, Set<String>> collectedMappings = new TreeMap<>(NULLS_LAST_STRING_COMPARATOR);

  protected List<String> ids;
  protected DeploymentMappings mappings;

  /**
   * Add mappings of deployment ids to resource ids to the overall element
   * mapping list.
   *
   * @param mappings
   *          the mappings to add
   */
  public void addDeploymentMappings(List<ImmutablePair<String, String>> mappings) {
    addDeploymentMappings(mappings, null);
  }

  /**
   * Add mappings of deployment ids to resource ids to the overall element
   * mapping list. All elements from <code>idList</code> that are not part of
   * the mappings will be added to the list of <code>null</code> deployment id
   * mappings.
   *
   * @param mappingsList
   *          the mappings to add
   * @param idList
   *          the list of ids to check for missing elements concerning the
   *          mappings to add
   */
  public void addDeploymentMappings(List<ImmutablePair<String, String>> mappingsList, Collection<String> idList) {
    if (ids != null) {
      ids = null;
      mappings = null;
    }
    Set<String> missingIds = idList == null ? null : new HashSet<>(idList);
    mappingsList.forEach(pair -> {
      String deploymentId = pair.getLeft();
      Set<String> idSet = collectedMappings.get(deploymentId);
      if (idSet == null) {
        idSet = new HashSet<>();
        collectedMappings.put(deploymentId, idSet);
      }
      idSet.add(pair.getRight());
      if (missingIds != null) {
        missingIds.remove(pair.getRight());
      }
    });
    // add missing ids to "null" deployment id
    if (missingIds != null && !missingIds.isEmpty()) {
      Set<String> nullIds = collectedMappings.get(null);
      if (nullIds == null) {
        nullIds = new HashSet<>();
        collectedMappings.put(null, nullIds);
      }
      nullIds.addAll(missingIds);
    }
  }

  /**
   * @return the list of ids that are mapped to deployment ids, ordered by
   *         deployment id
   */
  public List<String> getIds() {
    if (ids == null) {
      createDeploymentMappings();
    }
    return ids;
  }

  /**
   * @return the list of {@link DeploymentMapping}s
   */
  public DeploymentMappings getMappings() {
    if (mappings == null) {
      createDeploymentMappings();
    }
    return mappings;
  }

  public boolean isEmpty() {
    return collectedMappings.isEmpty();
  }

  protected void createDeploymentMappings() {
    ids = new ArrayList<>();
    mappings = new DeploymentMappings();

    for (Entry<String, Set<String>> mapping : collectedMappings.entrySet()) {
      ids.addAll(mapping.getValue());
      mappings.add(new DeploymentMapping(mapping.getKey(), mapping.getValue().size()));
    }
  }
}