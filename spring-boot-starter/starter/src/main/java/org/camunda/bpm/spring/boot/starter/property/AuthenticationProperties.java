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
package org.camunda.bpm.spring.boot.starter.property;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class AuthenticationProperties {

  @NestedConfigurationProperty
  protected CacheProperties cache = new CacheProperties();

  public CacheProperties getCache() {
    return cache;
  }

  public void setCache(CacheProperties cache) {
    this.cache = cache;
  }

  public static class CacheProperties {

    public static final long AUTH_CACHE_TIME_TO_LIVE = 1_000 * 60 * 5;

    /**
     * Enables authentication time to live.
     */
    protected boolean ttlEnabled = true;

    /**
     * Authentication time to live.
     */
    protected long timeToLive = AUTH_CACHE_TIME_TO_LIVE;

    public boolean isTtlEnabled() {
      return ttlEnabled;
    }

    public void setTtlEnabled(boolean ttlEnabled) {
      this.ttlEnabled = ttlEnabled;
    }

    public long getTimeToLive() {
      return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
      this.timeToLive = timeToLive;
    }

    @Override
    public String toString() {
      return joinOn(this.getClass())
          .add("ttlEnabled=" + ttlEnabled)
          .add("timeToLive=" + timeToLive)
          .toString();
    }

  }

}
