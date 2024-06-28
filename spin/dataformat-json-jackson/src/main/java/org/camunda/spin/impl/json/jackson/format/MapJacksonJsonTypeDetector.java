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
package org.camunda.spin.impl.json.jackson.format;

import org.camunda.spin.spi.TypeDetector;

import java.util.Map;

import static org.camunda.spin.impl.json.jackson.format.TypeHelper.constructType;

/**
 * Detects erased types of Map classes.
 * <p>To use it, make sure to call {@link JacksonJsonDataFormat#addTypeDetector(TypeDetector)}
 * to activate it.</p>
 */
public class MapJacksonJsonTypeDetector extends AbstractJacksonJsonTypeDetector {

    /**
     * Object instance to use.
     */
    public static MapJacksonJsonTypeDetector INSTANCE = new MapJacksonJsonTypeDetector();

    @Override
    public boolean canHandle(Object value) {
        return value instanceof Map<?, ?>;
    }

    @Override
    public String detectType(Object value) {
        return constructType(value).toCanonical();
    }
}
