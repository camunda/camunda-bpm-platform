'use strict';

var routeUtil = require('./../util/routeUtil');

/**
 * A wrapper to use the cockpit/util/routeUtil as a angular service

 * @name routeUtil
 * @memberof cam.cockpit.services
 * @type angular.service
 *
 * @module cam.cockpit.services.routeUtil
 */
module.exports = [function() {

  return {

    redirectToRuntime: function(params, currentPath, currentSearch) {
      return routeUtil.redirectToRuntime(params, currentPath, currentSearch);
    },

    replaceLastPathFragment: function(replacement, currentPath, currentSearch, keepSearchParams) {
      return routeUtil.replaceLastPathFragment(replacement, currentPath, currentSearch, keepSearchParams);
    },

    redirectTo: function(redirectUrl, currentSearch, keepSearchParams) {
      return routeUtil.redirectTo(redirectUrl, currentSearch, keepSearchParams);
    }
  };

}];
