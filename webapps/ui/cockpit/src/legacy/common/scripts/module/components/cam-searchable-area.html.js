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

module.exports = `<div cam-widget-search
     cam-widget-search-total="Searchable.pages.total"
     cam-widget-search-size="Searchable.pages.size"
     cam-widget-search-valid-searches="Searchable.config.searches"
     cam-widget-search-translations="Searchable.config.tooltips"
     cam-widget-search-types="Searchable.config.types"
     cam-widget-search-id="{{searchId}}"
     cam-widget-search-operators="Searchable.config.operators"
     cam-widget-search-storage-group="Searchable.storageGroup"
     cam-widget-search-mode="filter">
</div>
<div cam-widget-loader
     text-error="{{Searchable.loadingError}}"
     text-empty="{{Searchable.textEmpty}}"
     loading-state="{{Searchable.loadingState}}">

  <div ng-transclude></div>

  <ul uib-pagination ng-if="Searchable.pages.total > Searchable.pages.size"
              class="pagination-sm"
              page="Searchable.pages.current"
              ng-model="Searchable.pages.current"
              total-items="Searchable.pages.total"
              items-per-page="Searchable.pages.size"
              max-size="7"
              boundary-links="true">
  </ul>
</div>
`;
