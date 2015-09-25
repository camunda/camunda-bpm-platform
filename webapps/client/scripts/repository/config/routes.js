define([
  'text!./../controllers/cam-cockpit-repository-view.html'
], function(
  template) {
  'use strict';

  return [
    '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
      .when('/repository', {
        template: template,
        controller: 'camCockpitRepositoryViewCtrl',
        authentication: 'required',
        reloadOnSearch: false
      });
  }];
});
