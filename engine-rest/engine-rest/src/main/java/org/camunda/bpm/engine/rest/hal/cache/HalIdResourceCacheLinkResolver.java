/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.hal.cache;

import java.util.Comparator;

import org.camunda.bpm.engine.rest.hal.HalIdResource;
import org.camunda.bpm.engine.rest.hal.HalResource;

public abstract class HalIdResourceCacheLinkResolver extends HalCachingLinkResolver {

  public final static Comparator<HalResource<?>> ID_COMPARATOR = new HalIdResourceComparator();

  protected String getResourceId(HalResource<?> resource) {
    return ((HalIdResource) resource).getId();
  }

  @Override
  protected Comparator<HalResource<?>> getResourceComparator() {
    return ID_COMPARATOR;
  }

  public static class HalIdResourceComparator implements Comparator<HalResource<?>> {

    public int compare(HalResource<?> resource1, HalResource<?> resource2) {
      String id1 = ((HalIdResource) resource1).getId();
      String id2 = ((HalIdResource) resource2).getId();
      return id1.compareTo(id2);
    }

  }

}
