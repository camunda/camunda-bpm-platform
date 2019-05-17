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

/**
   * Interpolate a given dynamic name which contains markup. The result will be set
   * as the <code>name</code> attribute on the element.
   * @memberof cam.cockpit.directives
   * @name dynamicName
   * @type angular.Directive
   * @example
    <div ng-repeat="item in items">
      <input cam-dynamic-name="anElement{{ $index }}">
    </div>
    <!-- result -->
    <div>
      <input name="anElement0">
      <input name="anElement1">
      <input name="anElement2">
      ....
    </div>
   */
module.exports = [
  '$interpolate',
  '$compile',
  function($interpolate, $compile) {
    return {
      restrict: 'A',
      priority: 9999,
      terminal: true, //Pause Compilation
      link: function(scope, element, attr) {
        element.attr('name', $interpolate(attr.camDynamicName)(scope));

        //Resume compilation at priority 9999 so that our directive doesn't get re-compiled
        $compile(element, null, 9999)(scope);
      }
    };
  }
];
