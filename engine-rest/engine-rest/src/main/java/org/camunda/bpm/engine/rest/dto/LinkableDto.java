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
package org.camunda.bpm.engine.rest.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public abstract class LinkableDto {

  protected List<AtomLink> links = new ArrayList<AtomLink>();
  
  public List<AtomLink> getLinks() {
    return links;
  }
  
  public void addLink(AtomLink link) {
    links.add(link);
  }
  
  public void addReflexiveLink(URI linkUri, String method, String relation) {
    AtomLink link = generateLink(linkUri, method, relation);
    links.add(link);
  }
  
  public AtomLink generateLink(URI linkUri, String method, String relation) {   
    AtomLink link = new AtomLink(relation, linkUri.toString(), method);
    return link;
  }
  
}
