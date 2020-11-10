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

import React, { useState, useEffect } from "react";

let currentFilter = {};
let filterListeners = [];
let processData = null;

function handleFilterChange(newFilter) {
  if (JSON.stringify(newFilter) === JSON.stringify(currentFilter)) return;
  currentFilter = newFilter;
  filterListeners.forEach(setFilter => {
    setFilter(newFilter);
  });
}

function setNewFilter(newFilter) {
  currentFilter = newFilter;
  processData.set("filter", newFilter);
}
// This file mocks the behaviour of an HOC
// Currently, it only contains a bridge between Angular and React
// It will be replaced by an Context implementation when we remove Angular.
export function registerFilter(dataDepend) {
  dataDepend.observe("filter", handleFilterChange);
  processData = dataDepend;
}

const withFilter = Component => props => {
  const [filter, setFilter] = useState(currentFilter);

  useEffect(() => {
    // We don't have a react provider, so every Consumer needs a state
    // The state is updated when the dataDepend 'filter' object is changed
    filterListeners.push(setFilter);
    return () => {
      filterListeners = filterListeners.filter(val => val !== setFilter);
    };
  }, [setFilter]);

  return <Component filter={filter} setFilter={setNewFilter} {...props} />;
};

export default withFilter;
