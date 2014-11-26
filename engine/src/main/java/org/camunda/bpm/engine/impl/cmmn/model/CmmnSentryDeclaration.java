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
package org.camunda.bpm.engine.impl.cmmn.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnSentryDeclaration implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String PLAN_ITEM_ON_PART = "planItemOnPart";
  public static final String IF_PART = "ifPart";

  protected String id;

  protected Map<String, List<CmmnOnPartDeclaration>> onPartMap = new HashMap<String, List<CmmnOnPartDeclaration>>();
  protected List<CmmnOnPartDeclaration> onParts = new ArrayList<CmmnOnPartDeclaration>();

  protected CmmnIfPartDeclaration ifPart;

  public CmmnSentryDeclaration(String id) {
    this.id = id;
  }

  // id //////////////////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  // onParts ////////////////////////////////////////////////////////////

  public List<CmmnOnPartDeclaration> getOnParts() {
    return onParts;
  }

  public List<CmmnOnPartDeclaration> getOnParts(String sourceId) {
    return onPartMap.get(sourceId);
  }

  public void addOnPart(CmmnOnPartDeclaration onPart) {
    CmmnActivity source = onPart.getSource();
    if (source == null) {
      // do nothing: ignore onPart
      return;
    }

    String sourceId = source.getId();

    List<CmmnOnPartDeclaration> onPartDeclarations = onPartMap.get(sourceId);

    if (onPartDeclarations == null) {
      onPartDeclarations = new ArrayList<CmmnOnPartDeclaration>();
      onPartMap.put(sourceId, onPartDeclarations);
    }

    for (CmmnOnPartDeclaration onPartDeclaration : onPartDeclarations) {
      if (onPart.getStandardEvent().equals(onPartDeclaration.getStandardEvent())) {
        // if there already exists an onPartDeclaration which has the
        // same defined standardEvent then ignore this onPartDeclaration.

        if (onPartDeclaration.getSentry() == onPart.getSentry()) {
          return;
        }

        // but merge the sentryRef into the already existing onPartDeclaration
        if (onPartDeclaration.getSentry() == null && onPart.getSentry() != null) {
          // According to the specification, when "sentryRef" is specified,
          // "standardEvent" must have value "exit" (page 39, Table 23).
          // But there is no further check necessary.
          onPartDeclaration.setSentry(onPart.getSentry());
          return;
        }
      }
    }

    onPartDeclarations.add(onPart);
    onParts.add(onPart);

  }

  // ifPart //////////////////////////////////////////////////////////////////

  public CmmnIfPartDeclaration getIfPart() {
    return ifPart;
  }

  public void setIfPart(CmmnIfPartDeclaration ifPart) {
    this.ifPart = ifPart;
  }

}
