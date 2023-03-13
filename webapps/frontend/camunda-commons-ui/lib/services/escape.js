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
module.exports = function() {
  return function(str) {
    // see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent

    // we double escape the / character => / is escaped as '%2F',
    // we additionally escape '%' as '%25'
    return (
      encodeURIComponent(str)
        .replace(/%2F/g, '%252F')
        // !!!! could not found what "escape" is refering to, so I commented that !!!!
        // BTW, that RegExp looks... odd.
        // .replace(/[!'()]/g, escape)
        .replace(/\*/g, '%2A')
        .replace(/%5C/g, '%255C')
    );
  };
};
