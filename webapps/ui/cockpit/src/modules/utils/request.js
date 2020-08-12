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

import { getCSRFCookieName } from "utils/config";

const handlers = [];

export function put(url, body, options = {}) {
  return request({
    url,
    body,
    method: "PUT",
    ...options
  });
}

export function post(url, body, options = {}) {
  return request({
    url,
    body,
    method: "POST",
    ...options
  });
}

export function get(url, query, options = {}) {
  return request({
    url,
    query,
    method: "GET",
    ...options
  });
}

export function del(url, query, options = {}) {
  return request({
    url,
    query,
    method: "DELETE",
    ...options
  });
}

export function addHandler(fct, priority = 0) {
  handlers.push({ fct, priority });
  handlers.sort((a, b) => b.priority - a.priority);
}

export function removeHandler(fct) {
  handlers.splice(
    handlers.indexOf(handlers.find(entry => entry.fct === fct)),
    1
  );
}

export function getCSRFToken() {
  const CSRFCookieName = getCSRFCookieName();
  return document.cookie.replace(
    new RegExp(`(?:(?:^|.*;*)${CSRFCookieName}*=*([^;]*).*$)|^.*$`),
    "$1"
  );
}

export async function request(payload) {
  const { url, method, body, query, headers } = payload;
  const resourceUrl = query
    ? `${replaceApiPlaceholders(url)}?${formatQuery(query)}`
    : replaceApiPlaceholders(url);

  const XSRFToken = getCSRFToken();

  let requestHeaders = {
    "Content-Type": "application/json",
    "X-Authorized-Engine": "default",
    Accept: "application/json, text/plain, */*",
    ...headers
  };

  if (XSRFToken) {
    requestHeaders["X-XSRF-TOKEN"] = XSRFToken;
  }

  let response = await fetch(resourceUrl, {
    method,
    body: processBody(body),
    headers: requestHeaders,
    mode: "cors",
    credentials: "same-origin"
  });

  for (let i = 0; i < handlers.length; i++) {
    response = await handlers[i].fct(response, payload);
  }

  if (response.status >= 200 && response.status < 300) {
    return response;
  } else {
    throw response;
  }
}

export function formatQuery(query) {
  return Object.keys(query).reduce((queryStr, key) => {
    const value = query[key];

    if (Array.isArray(value)) {
      const str = value.map(val => `${key}=${val}`).join("&");
      if (!str) {
        return queryStr;
      }
      return queryStr === "" ? str : queryStr + "&" + str;
    }

    if (queryStr === "") {
      return `${key}=${encodeURIComponent(value)}`;
    }

    return `${queryStr}&${key}=${encodeURIComponent(value)}`;
  }, "");
}

function processBody(body) {
  if (typeof body === "string") {
    return body;
  }

  return JSON.stringify(body);
}

function replaceApiPlaceholders(url) {
  const base = document.querySelector("base");
  const engine = window.location.href.replace(/.*cockpit\/([^/]*).*/, "$1");

  return (
    url
      .replace("%ADMIN_API%", base.getAttribute("admin-api").slice(0, -1))
      .replace("%COCKPIT_API%", base.getAttribute("cockpit-api").slice(0, -1))
      .replace(
        "%ENGINE_API%",
        base.getAttribute("engine-api") + "engine/" + engine
      )
      .replace("%ENGINE%", engine)
      .replace("%API%", base.getAttribute("engine-api"))
      // Remove double slashes
      .replace(/([^:])(\/\/+)/g, "$1/")
  );
}
