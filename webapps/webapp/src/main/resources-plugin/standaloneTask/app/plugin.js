define([
  'angular',
  './navbar/main'
], function(
  angular,
  navbarModule
) {
  return angular.module('tasklist.plugin.standaloneTask', [navbarModule.name]);
});
