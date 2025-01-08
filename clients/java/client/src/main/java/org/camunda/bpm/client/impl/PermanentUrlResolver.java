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
package org.camunda.bpm.client.impl;

import org.camunda.bpm.client.UrlResolver;

/**
 * UrlResolver with permanent address
 */
public class PermanentUrlResolver implements UrlResolver {


    protected String baseUrl;

    public PermanentUrlResolver(String baseUrl) {
        this.setBaseUrl(baseUrl);
    }

    public void setBaseUrl(String baseUrl) {
        assert baseUrl != null && !baseUrl.isEmpty() : "camunda address can not be null or empty";
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }
}
