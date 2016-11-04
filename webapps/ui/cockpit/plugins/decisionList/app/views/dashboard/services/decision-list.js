'use strict';

module.exports = [
  '$q', 'camAPI',
  function($q, camAPI) {
    var decisionDefinitionService = camAPI.resource('decision-definition');
    var drdService = camAPI.resource('drd');

    return {
      getDecisionsLists: getDecisionsLists
    };

    function getDecisionsLists() {
      var decisions = decisionDefinitionService.list({
        latestVersion: true,
        sortBy: 'name',
        sortOrder: 'asc'
      });
      var drds = drdService.list({
        latestVersion: true,
        sortBy: 'name',
        sortOrder: 'asc'
      });

      return $q
        .all({
          decisions: decisions,
          drds: drds
        })
        .then(function(results) {
          results.decisions = connectDrdsToDecisionDefinitions(results.drds, results.decisions);

          return results;
        });
    }

    function connectDrdsToDecisionDefinitions(drds, decisions) {
      return decisions.map(function(decision) {
        if (decision.decisionRequirementsDefinitionId) {
          decision.drd = findDrdById(drds, decision.decisionRequirementsDefinitionId);
        }

        return decision;
      });
    }

    function findDrdById(drds, id) {
      return drds.filter(function(drd) {
        return drd.id === id;
      })[0];
    }
  }
];
