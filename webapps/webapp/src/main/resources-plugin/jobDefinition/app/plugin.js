define(['angular', './views/main', './data/main'],
function(angular, viewsModule, dataModule) {
  return angular.module('cockpit.plugin.jobDefinition', [viewsModule.name, dataModule.name]);
});
