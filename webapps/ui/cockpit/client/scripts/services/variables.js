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

var VariablesFactory = [
  '$translate',
  function($translate) {
    // variable specific stuff //////////////

    function reverse(hash) {
      var result = {};

      for (var key in hash) {
        result[hash[key]] = key;
      }

      return result;
    }

    function keys(hash) {
      var keys = [];

      for (var key in hash) {
        keys.push(key);
      }

      return keys;
    }

    var OPS = {
      eq: '=',
      neq: '!=',
      gt: '>',
      gteq: '>=',
      lt: '<',
      lteq: '<=',
      like: ' like '
    };

    var SYM_TO_OPS = reverse(OPS);

    function operatorName(op) {
      return OPS[op];
    }

    var PATTERN = new RegExp(
      '^(\\w+)\\s*(' + keys(SYM_TO_OPS).join('|') + ')\\s*([^!=<>]+)$'
    );

    /**
     * Tries to guess the type of the input string
     * and returns the appropriate representation
     * in the guessed type.
     *
     * @param value {string}
     * @return value {string|boolean|number} the interpolated value
     */
    function typed(value) {
      // is a string ( "asdf" )
      if (/^".*"\s*$/.test(value)) {
        return value.substring(1, value.length - 1);
      }

      if (parseFloat(value) + '' === value) {
        return parseFloat(value);
      }

      if (value === 'true' || value === 'false') {
        return value === 'true';
      }

      throw new Error(
        $translate.instant('VARIABLE_ERROR_INFER_TYPE', {value: value})
      );
    }

    function typedString(value) {
      if (!value) {
        return value;
      }

      if (typeof value === 'string') {
        return '"' + value + '"';
      }

      if (typeof value === 'boolean') {
        return value ? 'true' : 'false';
      }

      if (typeof value === 'number') {
        return value;
      }

      throw new Error(
        $translate.instant('VARIABLE_ERROR_INFER_TYPE', {value: value})
      );
    }

    /**
     * Public API of Variables utility
     */
    return {
      /**
       * Parse a string into a variableFilter { name: ..., operator: ..., value: ... }
       * @param  {string} str the string to parse
       * @return {object}     the parsed variableFilter object
       */
      parse: function(str) {
        var match = PATTERN.exec(str),
          value;

        if (!match) {
          throw new Error(
            $translate.instant('VARIABLE_ERROR_VARIABLE_SYNTAX', {message: str})
          );
        }

        value = typed(match[3]);

        return {
          name: match[1],
          operator: SYM_TO_OPS[match[2]],
          value: value
        };
      },

      toString: function(variable) {
        if (!variable) {
          return '';
        }

        return (
          variable.name +
          operatorName(variable.operator) +
          typedString(variable.value)
        );
      },

      operators: keys(SYM_TO_OPS)
    };
  }
];
module.exports = VariablesFactory;
