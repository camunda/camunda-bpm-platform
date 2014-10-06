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

package org.camunda.bpm.model.bpmn.instance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.fail;

/**
 * @author Sebastian Menski
 */
public class BoundaryEventTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CatchEvent.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("cancelActivity", false, false, true),
      new AttributeAssumption("attachedToRef", false, true)
    );
  }

  @Test
  public void shouldFailSettingCamundaAsyncBefore() {
    BoundaryEvent boundaryEvent = modelInstance.newInstance(BoundaryEvent.class);
    try {
      boundaryEvent.isCamundaAsyncBefore();
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }

    try {
      boundaryEvent.setCamundaAsyncBefore(false);
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }
  }

  @Test
  public void shouldFailSettingCamundaAsyncAfter() {
    BoundaryEvent boundaryEvent = modelInstance.newInstance(BoundaryEvent.class);
    try {
      boundaryEvent.isCamundaAsyncAfter();
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }

    try {
      boundaryEvent.setCamundaAsyncAfter(false);
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }
  }

  @Test
  public void shouldFailSettingCamundaExclusive() {
    BoundaryEvent boundaryEvent = modelInstance.newInstance(BoundaryEvent.class);
    try {
      boundaryEvent.isCamundaExclusive();
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }

    try {
      boundaryEvent.setCamundaExclusive(false);
      fail("Expected: UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // True
    }
  }
}
