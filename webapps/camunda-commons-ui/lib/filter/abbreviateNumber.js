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

var AbbreviateNumberFilter = function() {
  return function(number, decimal) {
    if (!number) {
      return;
    }

    if (number < 950) {
      return number;
    }

    if (!decimal) {
      decimal = 1;
    }

    return abbreviateNumber(number, decimal);
  };

  function abbreviateNumber(number, decimal) {
    // 2 decimal places => 100, 3 => 1000, etc
    decimal = Math.pow(10, decimal);

    // Enumerate number abbreviations according to https://en.wikipedia.org/wiki/Yotta-
    var abbreviations = ['k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'];

    // Go through the array backwards, so we do the largest first
    for (var i = abbreviations.length - 1; i >= 0; i--) {
      // Convert array index to "1000", "1000000", etc
      var size = Math.pow(10, (i + 1) * 3);

      // If the number is bigger or equal do the abbreviation
      if (size <= number) {
        // Here, we multiply by decimal, round, and then divide by decPlaces.
        // This gives us nice rounding to a particular decimal place.
        number = Math.round((number * decimal) / size) / decimal;

        // Handle special case where we round up to the next abbreviation
        if (number == 1000 && i < abbreviations.length - 1) {
          number = 1;
          i++;
        }

        // Add the letter for the abbreviation
        number += abbreviations[i];

        // We are done... stop
        return number;
      }
    }
    return number;
  }
};
module.exports = AbbreviateNumberFilter;
