/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.hal;

import org.camunda.bpm.engine.ProcessEngine;

import java.util.*;
import java.util.Map.Entry;

/**
 * A stateful linker which collects information about the links it creates.
 *
 * @author Daniel Meyer
 *
 */
public class HalLinker {

  /**
   * linked resource ids by {@link HalRelation}
   */
  Map<HalRelation, Set<String>> linkedResources = new HashMap<HalRelation, Set<String>>();

  protected final Hal hal;

  /**
   * The HalResource on which the links are constructed
   */
  protected final HalResource<?> resource;

  public HalLinker(Hal hal, HalResource<?> resource) {
    this.hal = hal;
    this.resource = resource;
  }

  /**
   * Creates a link in a given relation.
   *
   * @param rel the {@link HalRelation} for which a link should be constructed
   * @param pathParams the path params to populate the url template with.
   * */
  public void createLink(HalRelation rel, String... pathParams) {
    if(pathParams != null && pathParams.length > 0 && pathParams[0] != null) {
      Set<String> linkedResourceIds = linkedResources.get(rel);
      if(linkedResourceIds == null) {
        linkedResourceIds = new HashSet<String>();
        linkedResources.put(rel, linkedResourceIds);
      }

      // Hmm... use the last id in the pathParams as linked resource id
      linkedResourceIds.add(pathParams[pathParams.length - 1]);

      resource.addLink(rel.relName, rel.uriTemplate.build((Object[])pathParams));
    }
  }

  public Set<HalRelation> getLinkedRelations() {
    return linkedResources.keySet();
  }

  public Set<String> getLinkedResourceIdsByRelation(HalRelation relation) {
    Set<String> result = linkedResources.get(relation);
    if(result != null) {
      return result;
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Resolves a relation. Locates a HalLinkResolver for resolving the set of all linked resources in the relation.
   *
   * @param relation the relation to resolve
   * @param processEngine the process engine to use
   * @return the list of resolved resources
   * @throws RuntimeException if no HalLinkResolver can be found for the linked resource type.
   */
  public List<HalResource<?>> resolve(HalRelation relation, ProcessEngine processEngine) {
    HalLinkResolver linkResolver = hal.getLinkResolver(relation.resourceType);
    if(linkResolver != null) {
      Set<String> linkedIds = getLinkedResourceIdsByRelation(relation);
      if(!linkedIds.isEmpty()) {
        return linkResolver.resolveLinks(linkedIds.toArray(new String[linkedIds.size()]), processEngine);
      } else {
        return Collections.emptyList();
      }
    } else {
      throw new RuntimeException("Cannot find HAL link resolver for resource type '"+relation.resourceType+"'.");
    }
  }

  /**
   * merge the links of an embedded resource into this linker.
   * This is useful when building resources which are actually resource collections.
   * You can then merge the relations of all resources in the collection and the unique the set of linked resources to embed.
   *
   * @param embedded the embedded resource for which the links should be merged into this linker.
   */
  public void mergeLinks(HalResource<?> embedded) {
    for (Entry<HalRelation, Set<String>> linkentry : embedded.linker.linkedResources.entrySet()) {
      Set<String> linkedIdSet = linkedResources.get(linkentry.getKey());
      if(linkedIdSet != null) {
        linkedIdSet.addAll(linkentry.getValue());
      }else {
        linkedResources.put(linkentry.getKey(), linkentry.getValue());
      }
    }
  }
}
