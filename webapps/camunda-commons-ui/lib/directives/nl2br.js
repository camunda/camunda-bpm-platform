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

/**
 * Defines a directive who replaces plain text new lines with `<br />` HTML tags.
 *
 * Usage:
 *
 * Assuming the content of `scopeVarName` would be something like
 *
 * ```
 * First line of text yada.
 * Second line of the text and bamm.
 * ```
 *
 * and
 *
 * ```
 * <div nl2br="scopeVarName"></div>
 * ```
 *
 * will produce something like
 *
 * ```
 * <div nl2br="scopeVarName">First line of text yada.<br/>Second line of the text and bamm.</div>
 * ```
 */

'use strict';

// AngularJS DI
module.exports = [
  function() {
    return {
      scope: {
        original: '=nl2br'
      },

      link: function(scope, element) {
        // set the content as text (will eliminate malicious html characters)
        element.text(scope.original || '');

        // replace the line breaks
        var replaced = element.html().replace(/\n/g, '<br/>');

        // set the replaced content as html
        element.html(replaced);
      }
    };
  }
];
