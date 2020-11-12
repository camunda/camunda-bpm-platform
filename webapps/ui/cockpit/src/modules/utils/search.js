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

const getSearch = () => new URLSearchParams(window.location.hash.split("?")[1]);
const setSearch = search => {
  window.location.hash =
    window.location.hash.split("?")[0] + "?" + search.toString();
};

export default {
  get: key => {
    const locationSearch = getSearch();
    return locationSearch.get(key);
  },
  set: (key, value) => {
    const locationSearch = getSearch();
    locationSearch.set(key, value);

    setSearch(locationSearch);
  },
  delete: key => {
    const locationSearch = getSearch();
    locationSearch.delete(key);
    setSearch(locationSearch);
  }
};
