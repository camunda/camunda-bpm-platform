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

package org.camunda.bpm.engine.impl.cfg;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.camunda.bpm.engine.impl.cfg.ConfigurationFactory.EnvironmentVariable.enableXmlParsingCache;

public class ConfigurationFactory {

    private static final String KEY = "prefix";

    private static final Map<String, Configuration> CONFIG_BY_KEY = new HashMap<>();

    public static Configuration create(Reader reader, Properties properties, Environment environment) {
        String key = getKey(properties, environment);

        if (enableXmlParsingCache() && CONFIG_BY_KEY.containsKey(key)) {
            return CONFIG_BY_KEY.get(key);
        }

        XMLConfigBuilder parser = new XMLConfigBuilder(reader, "", properties);
        Configuration configuration = parser.getConfiguration();
        configuration.setEnvironment(environment);
        configuration = parser.parse();

        CONFIG_BY_KEY.put(key, configuration);

        return configuration;
    }

    private static String getKey(Properties properties, Environment environment) {
        return properties.get(KEY) + environment.getId();
    }

    static class EnvironmentVariable {

        public static boolean ENABLE_XML_PARSING_CACHE = true;

        public static boolean enableXmlParsingCache() {
            //TODO use Environment variable as external config to control cache
            return ENABLE_XML_PARSING_CACHE;
        }

    }


}
