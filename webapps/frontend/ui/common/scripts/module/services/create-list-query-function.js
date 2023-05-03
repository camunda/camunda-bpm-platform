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

'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$q',
  function($q) {
    return function(getCount, getList) {
      return function(query, pages, sorting) {
        return getCount(query).then(function(data) {
          var first = (pages.current - 1) * pages.size;
          var count = data.count;
          var listQuery = angular.extend(
            {},
            query,
            {
              firstResult: first,
              maxResults: pages.size
            },
            sorting
          );

          if (count > first) {
            return $q.all({
              count: count,
              list: getList(listQuery)
            });
          }

          return data;
        });
      };
    };
  }
];
