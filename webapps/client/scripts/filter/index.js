'use strict';



/**
 * @module  cam.tasklist.filter
 * @belongsto cam.tasklist
 *
 * Filters are predefined filters for tasks.
 */



define([
  'require',
  'angular',
  'moment',
  './directives/cam-tasklist-filters',
  './controller/cam-edit-filter-ctrl',
  './modals/cam-tasklist-filter-form',
  'camunda-tasklist-ui/api'
], function(
  require,
  angular,
  moment,
  camTasklistFilters,
  camEditFilterCtrl,
  camTasklistFilterForm,
  api
) {

  var filterModule = angular.module('cam.tasklist.filter', [
    api.name,
    'ui.bootstrap',
    'cam.widget',
    'angularMoment'
  ]);

  filterModule.controller('camEditFilterCtrl', camEditFilterCtrl);

  filterModule.controller('camEditFilterModalCtrl', camTasklistFilterForm);

  filterModule.directive('camTasklistFilters', camTasklistFilters);

  return filterModule;
});
