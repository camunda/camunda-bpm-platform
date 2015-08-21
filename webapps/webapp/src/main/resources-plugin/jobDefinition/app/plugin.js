define(['angular', './views/main', './data/main', './actions/main'],
function(angular, viewsModule, dataModule, actionsModule) {
  return angular.module('cockpit.plugin.jobDefinition', [viewsModule.name, dataModule.name, actionsModule.name]);
});
