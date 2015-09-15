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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Thorben Lindhauer
 *
 */
public class QueryValidators {

  public static class AdhocQueryValidator<T extends AbstractQuery<?, ?>> implements Validator<T> {

    @SuppressWarnings("rawtypes")
    public static final AdhocQueryValidator INSTANCE = new AdhocQueryValidator();

    private AdhocQueryValidator() {
    }

    @Override
    public void validate(T query) {
      if (!Context.getProcessEngineConfiguration().isEnableExpressionsInAdhocQueries() &&
          !query.getExpressions().isEmpty()) {
        throw new BadUserRequestException("Expressions are forbidden in adhoc queries. This behavior can be toggled"
            + " in the process engine configuration");
      }
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractQuery<?, ?>> AdhocQueryValidator<T> get() {
      return (AdhocQueryValidator<T>) INSTANCE;
    }

  }

  public static class StoredQueryValidator<T extends AbstractQuery<?, ?>> implements Validator<T> {

    @SuppressWarnings("rawtypes")
    public static final StoredQueryValidator INSTANCE = new StoredQueryValidator();

    private StoredQueryValidator() {
    }

    @Override
    public void validate(T query) {
      if (!Context.getProcessEngineConfiguration().isEnableExpressionsInStoredQueries() &&
          !query.getExpressions().isEmpty()) {
        throw new BadUserRequestException("Expressions are forbidden in stored queries. This behavior can be toggled"
            + " in the process engine configuration");
      }
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractQuery<?, ?>> StoredQueryValidator<T> get() {
      return (StoredQueryValidator<T>) INSTANCE;
    }
  }

}
