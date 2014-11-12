'use strict';



/**
 * @module  cam.tasklist.filter
 * @belongsto cam.tasklist
 *
 * Filters are predefined filters for tasks.
 */



define([
  'angular',

  /* directives */
  './directives/cam-tasklist-filters',
  './directives/cam-tasklist-filter-modal-form',
  './directives/cam-tasklist-filter-modal-form-general',
  './directives/cam-tasklist-filter-modal-form-criteria',
  './directives/cam-tasklist-filter-modal-form-variable',
  './directives/cam-tasklist-filter-modal-form-permission',

  /* controllers */
  './controllers/cam-tasklist-filters-ctrl',

  /* modals */
  './modals/cam-tasklist-filter-modal',

], function(
  angular,

  /* directives */
  camTasklistFilters,
  camTasklistFilterModalForm,
  camTasklistFilterModalFormGeneral,
  camTasklistFilterModalFormCriteria,
  camTasklistFilterModalFormVariable,
  camTasklistFilterModalFormPermission,

  /* controllers */
  camTasklistFiltersCtrl,

  /* modals */
  camTasklistFilterModal
) {

  var filterModule = angular.module('cam.tasklist.filter', [
    'ui.bootstrap',
  ]);

  /* directives */
  filterModule.directive('camTasklistFilters', camTasklistFilters);
  filterModule.directive('camTasklistFilterModalForm', camTasklistFilterModalForm);
  filterModule.directive('camTasklistFilterModalFormGeneral', camTasklistFilterModalFormGeneral);
  filterModule.directive('camTasklistFilterModalFormCriteria', camTasklistFilterModalFormCriteria);
  filterModule.directive('camTasklistFilterModalFormVariable', camTasklistFilterModalFormVariable);
  filterModule.directive('camTasklistFilterModalFormPermission', camTasklistFilterModalFormPermission);

  /* controllers */
  filterModule.controller('camFiltersCtrl', camTasklistFiltersCtrl);


  /* modals */
  filterModule.controller('camFilterModalCtrl', camTasklistFilterModal);

  return filterModule;
});
