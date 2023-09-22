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
  return {
    restrict: 'EAC',
    link: function(scope, element, attrs) {
      element.addClass('circle');

      scope.$watch(attrs.incidents, function() {
        updateStateCircle();
      });

      scope.$watch(attrs.incidentscount, function() {
        updateStateCircle();
      });

      scope.$watch(attrs.running, function() {
        updateStateCircle();
      });

      function updateStateCircle() {
        var incidents = scope.$eval(attrs.incidents);
        var running = scope.$eval(attrs.running);
        var incidentsForTypes = scope.$eval(attrs.incidentsForTypes) || [];
        var incidentsCount = scope.$eval(attrs.incidentscount) || null;

        if (running) {
          return setStateToBlue();
        }

        if (incidentsCount > 0) {
          setStateToRed();
          return;
        }

        if (!!incidents && incidents.length > 0) {
          // In that case 'incidentsForTypes.length === 0' means
          // that the state has to be set to red independent
          // from the incident type.
          // Note: incidents.length is greater than zero.
          if (incidentsForTypes.length === 0) {
            setStateToRed();
            return;
          }

          // In the other case we check whether there exist
          // at least one incident to one of the incident types.
          for (var i = 0; i < incidents.length; i++) {
            var incident = incidents[i];

            if (incident.incidentType.indexOf(incidentsForTypes) != -1) {
              if (incident.incidentCount > 0) {
                setStateToRed();
                return;
              }
            }
          }
        }
        // If there does not exist any incident, the state is green.
        setStateToGreen();
      }

      function setStateToGreen() {
        element
          .removeClass('circle-red')
          .removeClass('circle-blue')
          .removeClass('animate-spin')
          .addClass('circle-green');
      }

      function setStateToRed() {
        element
          .removeClass('circle-green')
          .removeClass('circle-blue')
          .removeClass('animate-spin')
          .addClass('circle-red');
      }

      function setStateToBlue() {
        element
          .removeClass('circle-green')
          .removeClass('circle-red')
          .addClass('circle-blue')
          .addClass('animate-spin');
      }
    }
  };
};
