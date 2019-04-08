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
package org.camunda.bpm.client.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class TestUtil {

  public static int MAX_RETRIES = 100;

  public static <T> Invocation<T> doRepeatedly(Callable<T> callable) {
    return new Invocation<>(callable);
  }

  public static Invocation<Void> doRepeatedly(Runnable runnable) {
    return new Invocation<>(() -> {
      runnable.run();
      return null;
    });
  }

  public static void waitUntil(BooleanSupplier condition) {
    doRepeatedly(() -> null).until((r) -> condition.getAsBoolean());
  }

  public static void waitUntil(BooleanSupplier condition, String message, Object... args) {
    doRepeatedly(() -> null).until((r) -> condition.getAsBoolean(), message, args);
  }

  public static void waitUntil(BooleanSupplier condition, int retries) {
    doRepeatedly(() -> null).until((r) -> condition.getAsBoolean(), retries);
  }

  public static void waitUntil(BooleanSupplier condition, int retries, String message, Object... args) {
    doRepeatedly(() -> null).until((r) -> condition.getAsBoolean(), retries, message, args);
  }

  public static class Invocation<T> {
    protected Callable<T> callable;

    public Invocation(Callable<T> callable) {
      this.callable = callable;
    }

    public T until(Function<T, Boolean> resultCondition) {
      return until(resultCondition, (e) -> false);
    }

    public T until(Function<T, Boolean> resultCondition, String message, Object... args) {
      return until(resultCondition, (e) -> false, message, args);
    }

    public T until(Function<T, Boolean> resultCondition, int retries) {
      return until(resultCondition, (e) -> false, retries);
    }

    public T until(Function<T, Boolean> resultCondition, int retries, String message, Object... args) {
      return until(resultCondition, (e) -> false, retries, message, args);
    }

    public T until(Function<T, Boolean> resultCondition, Function<Exception, Boolean> exceptionCondition) {
      T result = whileConditionHolds((t) -> !resultCondition.apply(t), (e) -> !exceptionCondition.apply(e));

      assertThat(resultCondition.apply(result)).isTrue();

      return result;
    }

    public T until(Function<T, Boolean> resultCondition, Function<Exception, Boolean> exceptionCondition, String message, Object... args) {
      T result = whileConditionHolds((t) -> !resultCondition.apply(t), (e) -> !exceptionCondition.apply(e));

      assertThat(resultCondition.apply(result)).withFailMessage(message, args).isTrue();

      return result;
    }

    public T until(Function<T, Boolean> resultCondition, Function<Exception, Boolean> exceptionCondition, int retries) {
      T result = whileConditionHolds((t) -> !resultCondition.apply(t), (e) -> !exceptionCondition.apply(e), retries);

      assertThat(resultCondition.apply(result)).isTrue();

      return result;
    }

    public T until(Function<T, Boolean> resultCondition, Function<Exception, Boolean> exceptionCondition, int retries, String message,
        Object... args) {
      T result = whileConditionHolds((t) -> !resultCondition.apply(t), (e) -> !exceptionCondition.apply(e), retries);

      assertThat(resultCondition.apply(result)).withFailMessage(message, args).isTrue();

      return result;
    }

    public T whileConditionHolds(Function<T, Boolean> resultCondition) {
      return whileConditionHolds(resultCondition, (e) -> true);
    }

    public T whileConditionHolds(Function<T, Boolean> resultCondition, int retires) {
      return whileConditionHolds(resultCondition, (e) -> true, retires);
    }

    public T whileConditionHolds(Function<T, Boolean> resultCondition, Function<Exception, Boolean> exceptionCondition) {
      return whileConditionHolds(resultCondition, exceptionCondition, MAX_RETRIES);
    }

    public T whileConditionHolds(Function<T, Boolean> resultCondition, Function<Exception, Boolean> exceptionCondition, int retries) {
      int numTries = 0;

      T result;

      do {
        result = null;

        try {
          if (numTries > 0) {
            Thread.sleep(100L);
          }

          result = callable.call();
        } catch (Exception e) {
          if (!exceptionCondition.apply(e)) {
            throw new RuntimeException("Unexpected exception while checking condition", e);
          }
        }

        numTries++;
      } while (numTries < retries && resultCondition.apply(result));

      return result;
    }
  }
}
