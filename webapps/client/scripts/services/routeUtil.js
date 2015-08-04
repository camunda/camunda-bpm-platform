/* global define: false */
define([
  'cockpit/util/routeUtil'
],

/**
 * A wrapper to use the cockpit/util/routeUtil as a angular service

 * @name routeUtil
 * @memberof cam.cockpit.services
 * @type angular.service
 *
 * @module cam.cockpit.services.routeUtil
 */
function(routeUtil) {
  'use strict';

  return [function() {

    return {

      redirectToRuntime: function(params, currentPath, currentSearch) {
        return routeUtil.redirectToRuntime(params, currentPath, currentSearch);
      },

      replaceLastPathFragment: function (replacement, currentPath, currentSearch, keepSearchParams) {
        return routeUtil.replaceLastPathFragment(replacement, currentPath, currentSearch, keepSearchParams);
      },

      redirectTo: function (redirectUrl, currentSearch, keepSearchParams) {
        return routeUtil.redirectTo(redirectUrl, currentSearch, keepSearchParams);
      }
    }

  }];

});
