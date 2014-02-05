/* global ngDefine: false */

ngDefine('camunda.common.services.breadcrumbs', ['angular'], function(module, angular) {
  'use strict';

  /**
   * A service to manage a page breadcrumbs.
   *
   * @name breadcrumbs
   * @memberof cam.common.services
   * @type angular.service
   *
   * @module cam.common.services.breadcrumbs
   */
  module.service('breadcrumbs', [
    '$rootScope',
  function($rootScope) {
    $rootScope.page = $rootScope.page || {};
    $rootScope.page.breadcrumbs = $rootScope.page.breadcrumbs || [];

    return {
      /**
       * Adds one or more breadcrumbs
       *
       * @param crumb {(Object.<string, *>|Object[])} crumb - a breadcrumb object or an array of breadcrumb objects
       * @returns this {angular.Service}
       */
      add: function(crumb) {
        if (angular.isArray(crumb)) {
          return angular.forEach(crumb, this.add);
        }

        $rootScope.page.breadcrumbs.push(crumb);
        // $rootScope.page.breadcrumbs.push(extendCrumb(crumb));
        return this;
      },

      /**
       * Get the breadcrumbs
       *
       * @returns {Array} - An array of breadcrumb objects
       */
      get: function() {
        return $rootScope.page.breadcrumbs;
      },

      /**
       * Clears the breadcrumb (remove all items)
       *
       * @returns this {angular.Service}
       */
      clear: function() {
        $rootScope.page.breadcrumbs = [];
        return this;
      }
    };
  }]);
});
