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

module.exports = [
  'ProcessDefinitionResource',
  'page',
  function(ProcessDefinitionResource, page) {
    function breadcrumbTrails(
      processInstance,
      fetchSuperInstance,
      trail,
      index,
      urlSuffix
    ) {
      trail = trail || [];

      function handleSuperProcessInstance(err, superProcessInstance) {
        if (!superProcessInstance) {
          page.breadcrumbsInsertAt(index, trail);
          return;
        }

        // ... and fetch its process definition
        ProcessDefinitionResource.get({
          // TODO: CAM-2017 API definition cleanup
          id:
            superProcessInstance.processDefinitionId ||
            superProcessInstance.definitionId
        })
          .$promise.then(function(response) {
            // var superProcessDefinition = response.data;
            var superProcessDefinition = response;

            // ... PREpend the breadcrumbs
            trail = [
              {
                href:
                  '#/process-definition/' +
                  superProcessDefinition.id +
                  (urlSuffix ? '/' + urlSuffix : ''),
                label: superProcessDefinition.name || superProcessDefinition.key
              },
              {
                divider: ':',
                href:
                  '#/process-instance/' +
                  superProcessInstance.id +
                  (urlSuffix ? '/' + urlSuffix : ''),
                label: superProcessInstance.id.slice(0, 8) + '…'
              }
            ].concat(trail);

            breadcrumbTrails(
              superProcessInstance,
              fetchSuperInstance,
              trail,
              index,
              urlSuffix
            );
          })
          .catch(function() {});
      }

      fetchSuperInstance(processInstance, handleSuperProcessInstance);
    }

    return breadcrumbTrails;
  }
];
