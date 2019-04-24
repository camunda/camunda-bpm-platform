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
package org.camunda.bpm.engine.rest.hal;

import org.camunda.bpm.engine.ProcessEngine;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Base class for implementing a HAL resource as defined in
 * <a href="http://tools.ietf.org/html/draft-kelly-json-hal-06#section-4">json-hal-06#section-4</a>
 *
 * @author Daniel Meyer
 *
 */
public abstract class HalResource<T extends HalResource<?>> {

  /** This resource links */
  protected Map<String, HalLink> _links;

  /** Embedded resources */
  protected Map<String, Object> _embedded;

  // the linker used by this resource
  protected transient HalLinker linker;

  public HalResource() {
    this.linker = Hal.getInstance().createLinker(this);
  }

  public Map<String, HalLink> get_links() {
    return _links;
  }

  public Map<String, Object> get_embedded() {
    return _embedded;
  }

  public void addLink(String rel, String href) {
    if(_links == null) {
      _links = new TreeMap<String, HalLink>();
    }
    _links.put(rel, new HalLink(href));
  }

  public void addLink(String rel, URI hrefUri) {
    addLink(rel, hrefUri.toString());
  }

  public void addEmbedded(String name, HalResource<?> embedded) {
    linker.mergeLinks(embedded);
    addEmbeddedObject(name, embedded);
  }

  private void addEmbeddedObject(String name, Object embedded) {
    if(_embedded == null) {
      _embedded = new TreeMap<String, Object>();
    }
    _embedded.put(name, embedded);
  }

  public void addEmbedded(String name, List<HalResource<?>> embeddedCollection) {
    for (HalResource<?> resource : embeddedCollection) {
      linker.mergeLinks(resource);
    }
    addEmbeddedObject(name, embeddedCollection);
  }

  public Object getEmbedded(String name) {
    return _embedded.get(name);
  }

  /**
   * Can be used to embed a relation. Embedded all linked resources in the given relation.
   *
   * @param relation the relation to embedded
   * @param processEngine used to resolve the resources
   * @return the resource itself.
   */
  @SuppressWarnings("unchecked")
  public T embed(HalRelation relation, ProcessEngine processEngine) {
    List<HalResource<?>> resolvedLinks = linker.resolve(relation, processEngine);
    if(resolvedLinks != null && resolvedLinks.size() > 0) {
      addEmbedded(relation.relName, resolvedLinks);
    }
    return (T) this;
  }

}
