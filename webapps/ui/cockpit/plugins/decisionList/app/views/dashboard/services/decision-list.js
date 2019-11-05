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
  '$q',
  'camAPI',
  function($q, camAPI) {
    var decisionDefinitionService = camAPI.resource('decision-definition');
    var drdService = camAPI.resource('drd');

    var drds, decisions;

    var defaultParams = {
      latestVersion: true,
      sortBy: 'name',
      sortOrder: 'asc',
      firstResult: 0,
      maxResults: 50
    };

    function getDecisions(params) {
      return decisionDefinitionService
        .list(Object.assign({}, defaultParams, params))
        .then(function(result) {
          decisions = result;

          if (drds) result = connectDrdsToDecisionDefinitions(drds, result);

          return result;
        });
    }

    function getDrds(params) {
      return drdService
        .list(Object.assign({}, defaultParams, params))
        .then(function(result) {
          drds = result;

          if (decisions)
            result = connectDrdsToDecisionDefinitions(drds, result);

          return result;
        });
    }

    function getDecisionsLists(decParams, drdParams) {
      var decisionsProm = decisionDefinitionService.list(
        Object.assign({}, defaultParams, decParams)
      );

      var decisionsCountProm = decisionDefinitionService.count({
        latestVersion: true
      });

      var drdsProm = drdService.list(
        Object.assign({}, defaultParams, drdParams)
      );

      var drdsCountProm = drdService.count({
        latestVersion: true
      });

      return $q
        .all({
          decisions: decisionsProm,
          decisionsCount: decisionsCountProm,
          drds: drdsProm,
          drdsCount: drdsCountProm
        })
        .then(function(results) {
          drds = results.drds;
          decisions = results.decisions;

          decisions = results.decisions = connectDrdsToDecisionDefinitions(
            results.drds,
            results.decisions
          );
          results.drdsCount = results.drdsCount.count;

          return results;
        })
        .catch(function() {});
    }

    function connectDrdsToDecisionDefinitions(drds, decisions) {
      return decisions.map(function(decision) {
        if (decision.decisionRequirementsDefinitionId) {
          decision.drd = findDrdById(
            drds,
            decision.decisionRequirementsDefinitionId
          ) || {
            key: decision.decisionRequirementsDefinitionKey,
            id: decision.decisionRequirementsDefinitionId
          };
        }

        return decision;
      });
    }

    function findDrdById(drds, id) {
      return drds.filter(function(drd) {
        return drd.id === id;
      })[0];
    }

    return {
      getDecisionsLists: getDecisionsLists,
      getDecisions: getDecisions,
      getDrds: getDrds
    };
  }
];
