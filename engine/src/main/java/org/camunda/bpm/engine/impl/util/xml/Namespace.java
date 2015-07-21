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
package org.camunda.bpm.engine.impl.util.xml;

/**
 * @author Ronny Bräunlich
 *
 */
public class Namespace {

  private final String namespaceUri;
  private final String alternativeUri;

  public Namespace(String namespaceUri) {
    this(namespaceUri, null);
  }

  /**
   * Creates a namespace with an alternative uri.
   *
   * @param namespaceUri
   * @param alternativeUri
   */
  public Namespace(String namespaceUri, String alternativeUri) {
    this.namespaceUri = namespaceUri;
    this.alternativeUri = alternativeUri;
  }

  /**
   * If a namespace has changed over time it could feel responsible for handling
   * the older one.
   *
   * @return
   */
  public boolean hasAlternativeUri() {
    return alternativeUri != null;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getAlternativeUri() {
    return alternativeUri;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((namespaceUri == null) ? 0 : namespaceUri.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Namespace other = (Namespace) obj;
    if (namespaceUri == null) {
      if (other.namespaceUri != null)
        return false;
    } else if (!namespaceUri.equals(other.namespaceUri))
      return false;
    return true;
  }

}
