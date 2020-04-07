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

import buildInPlugins from "../../plugins";
import eePlugins from "../../enterprise";

const inProduction = process.env.NODE_ENV === "production";

const configPath = inProduction ? "../config.json" : "/config.json";
let config = {};

function getLanguage() {
  const nav = window.navigator;
  const browserLang = (
    (Array.isArray(nav.languages)
      ? nav.languages[0]
      : nav.language ||
        nav.browserLanguage ||
        nav.systemLanguage ||
        nav.userLanguage) || ""
  ).split("-");

  return browserLang[0].toLowerCase();
}

function addCssSource(url) {
  var link = document.createElement("link");
  link.rel = "stylesheet";
  link.type = "text/css";
  link.href = url;
  document.head.appendChild(link);
}

const getExcludedPlugins = () => {
  const excludeString =
    document.querySelector("base").getAttribute("cam-exclude-plugins") || "";

  const excluded = excludeString.split(",").map(element => {
    let pluginPoint = element.trim().split(":");
    if (pluginPoint.length < 2) pluginPoint.push("*");
    return pluginPoint;
  });
  return excluded;
};

async function loadPlugins() {
  const excludedPlugins = getExcludedPlugins();
  const excludedPluginsFilter = plugin => {
    return !excludedPlugins.some(
      el =>
        plugin.pluginPoint === el[0] && (el[1] === "*" || el[1] === plugin.id)
    );
  };

  const customScripts = config.customScripts;
  const JARScripts = window.JAR_PLUGINS.map(el => {
    addCssSource(`${el.location}/plugin.css`);
    return `${el.location}/${el.main}`;
  });

  const fetchers = customScripts.map(url =>
    import(/* webpackIgnore: true */ "../../" + url)
  );

  fetchers.push(
    ...JARScripts.map(url => import(/* webpackIgnore: true */ url))
  );

  const loadedPlugins = (await Promise.all(fetchers)).reduce((acc, module) => {
    const plugins = module.default;
    if (Array.isArray(plugins)) {
      acc.push(...plugins);
    } else {
      acc.push(plugins);
    }
    return acc;
  }, []);

  const combinedPlugins = [...buildInPlugins, ...eePlugins, ...loadedPlugins];
  config.plugins = combinedPlugins.filter(excludedPluginsFilter);
}

async function loadLocale() {
  const locales = config.locales;
  const preferredLanguage = getLanguage();
  const defaultLanguage = locales[0];
  const localesToLoad = locales.includes(preferredLanguage)
    ? [defaultLanguage, preferredLanguage] // Order is important, defaultLanguage always first
    : [defaultLanguage];

  const loadedLocales = await Promise.all(
    localesToLoad.map(
      async local =>
        await (
          await fetch(
            inProduction ? `../locales/${local}.json` : `/locales/${local}.json`
          )
        ).json()
    )
  );

  if (!locales.includes(preferredLanguage)) {
    config.locale = loadedLocales[0];
    return;
  }
  const defaultLocale = loadedLocales[0];
  const preferredLocale = loadedLocales[1];
  for (let key in defaultLocale) {
    preferredLocale[key] = preferredLocale[key]
      ? { ...defaultLocale[key], ...preferredLocale[key] }
      : defaultLocale[key];
  }

  config.locale = preferredLocale;
}

async function loadBpmnJsExtensions() {
  const bpmnJsConf = config["bpmnJs"] || {};

  const moduleFetchers = bpmnJsConf.additionalModules.map(url =>
    import(/* webpackIgnore: true */ "../../" + url)
  );

  const modulePromise = Promise.all(moduleFetchers).then(result => {
    bpmnJsConf.loadedModules = result.map(module => module.default);
  });

  // moddleExtensions
  const moddlePromises = [];

  for (const key in bpmnJsConf.moddleExtensions) {
    const path = bpmnJsConf.moddleExtensions[key];
    moddlePromises.push(
      fetch(inProduction ? `../${path}.json` : `/${path}.json`)
        .then(res => res.json())
        .then(json => {
          bpmnJsConf.moddleExtensions[key] = json;
        })
    );
  }

  return await Promise.all([modulePromise, ...moddlePromises]);
}

export async function loadConfig() {
  config = await (await fetch(configPath)).json();
  await Promise.all([loadPlugins(), loadLocale(), loadBpmnJsExtensions()]);
  return config;
}

export const getConfig = () => config;
export const getLocale = () => config["locale"];
export const getPlugins = () => config["plugins"];
export const getCSRFCookieName = () => config["csrfCookieName"];

export default config;
