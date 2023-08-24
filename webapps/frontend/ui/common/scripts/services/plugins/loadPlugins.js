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

const appRoot = document.querySelector('base').getAttribute('app-root');

const camundaPlugins = [
  'admin-plugin-adminPlugins',
  'admin-plugin-adminEE',
  'cockpit-plugin-cockpitPlugins',
  'cockpit-plugin-cockpitEE',
  'tasklist-plugin-tasklistPlugins'
];

function withSuffix(string, suffix) {
  return !string.endsWith(suffix) ? string + suffix : string;
}

function addCssSource(url) {
  var link = document.createElement('link');
  link.rel = 'stylesheet';
  link.type = 'text/css';
  link.href = url;
  document.head.appendChild(link);
}

module.exports = async function loadPlugins(config, appName) {
  const customScripts = config.customScripts || [];

  const JARScripts = window.PLUGIN_PACKAGES.filter(
    el =>
      !camundaPlugins.includes(el.name) &&
      !el.name.startsWith(`${appName}-plugin-legacy`)
  ).map(el => {
    addCssSource(`${el.location}/plugin.css?bust=${CAMUNDA_VERSION}`); // eslint-disable-line
    return `${el.location}/${el.main}?bust=${CAMUNDA_VERSION}`; // eslint-disable-line
  });

  const baseImportPath = `${appRoot}/app/${appName}/`;
  const fetchers = customScripts.map(url =>
    // eslint-disable-next-line
    _import(
      baseImportPath + withSuffix(url, '.js') + `?bust=${CAMUNDA_VERSION}` // eslint-disable-line
    ).catch(
      e => console.error(e) // eslint-disable-line
    )
  );

  fetchers.push(
    ...JARScripts.map(url => {
      return _import(url).catch(e => console.error(e)); // eslint-disable-line
    })
  );

  return (await Promise.all(fetchers)).reduce((acc, module) => {
    const plugins = module.default;
    if (!plugins) {
      return acc;
    }

    if (Array.isArray(plugins)) {
      acc.push(...plugins);
    } else {
      acc.push(plugins);
    }
    return acc;
  }, []);
};
