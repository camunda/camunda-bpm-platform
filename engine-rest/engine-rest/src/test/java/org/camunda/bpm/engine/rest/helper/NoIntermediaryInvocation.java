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
package org.camunda.bpm.engine.rest.helper;

import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.internal.verification.api.VerificationDataInOrder;
import org.mockito.internal.verification.api.VerificationInOrderMode;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

/**
 * Stricter verification mode than regular Mockito inOrder verification by requiring
 * the given invocation to occur directly after the last verified invocation. May be
 * useful to verify APIs where order is important, e.g. fluent builders.
 *
 * @author Thorben Lindhauer
 */
public class NoIntermediaryInvocation implements VerificationInOrderMode, VerificationMode {

  protected InvocationsFinder finder = new InvocationsFinder();

  @Override
  public void verifyInOrder(VerificationDataInOrder data) {

    Invocation firstUnverifiedInvocation = finder.findFirstUnverifiedInOrder(data.getOrderingContext(), data.getAllInvocations());

    if (firstUnverifiedInvocation == null) {
      Invocation previouslyVerified = finder.findPreviousVerifiedInOrder(data.getAllInvocations(), data.getOrderingContext());
      new Reporter().wantedButNotInvokedInOrder(data.getWanted(), previouslyVerified);
    }

    if (!data.getWanted().matches(firstUnverifiedInvocation)) {
      StringBuilder sb = new StringBuilder();
      sb.append("Expected next invocation specified here: \n");
      sb.append(data.getWanted().getLocation());
      sb.append("\n");
      sb.append("but next invocation was: \n");
      sb.append(firstUnverifiedInvocation.getLocation());
      sb.append("\n");

      throw new MockitoAssertionError(sb.toString());
    }
  }

  @Override
  public void verify(VerificationData data) {
    throw new RuntimeException("Applies only to inorder verification");
  }

  public static final NoIntermediaryInvocation immediatelyAfter() {
    return new NoIntermediaryInvocation();
  }

}
