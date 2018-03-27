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
package org.camunda.bpm.client.impl;

import javax.xml.bind.annotation.XmlElement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class XmlList extends AbstractList<Integer> {

  @XmlElement(name="e")
  private final List<Integer> list;

  public XmlList() {
    this.list = new ArrayList<>();
  }

  public XmlList(List<Integer> list) {
    this.list = new ArrayList<>(list);
  }

  @Override
  public Integer get(int index) {
    return list.get(index);
  }

  @Override
  public boolean add(Integer e) {
    return list.add(e);
  }

  @Override
  public int size() {
    return list.size();
  }

}