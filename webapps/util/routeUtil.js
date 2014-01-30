/* global define: false */
define([], function() {
  'use strict';
  /**
   * A set of utilities for routing
   * @exports routeUtil
   */
  var utils = {};

  /**
   * Determine where (and if) the user should be redirected
   * when landing on a the `process-definition` or `process-instance`
   * base URL.
   * {@link http://code.angularjs.org/1.1.5/docs/api/ng.$routeProvider|see ng.$routeProvider documentation}
   *
   * @param {Object} params         - The route parameters as used in
   *                                  a angular route
   * @param {string} currentPath    - The actual path of the request,
   *                                  generally something like:
   *                                  `/process-definition/<processDefinitionId>`
   * @param {Object} currentSearch  - The parsed object of the route "search" part
   * @return {string}               - A URL string to be redirected to
   */
  utils.redirectToLive = function(params, currentPath, currentSearch) {
    var redirectUrl = currentPath + '/live',
        search = [],
        key;

    for (key in currentSearch) {
      search.push(key + '=' + currentSearch[key]);
    }

    return redirectUrl + (search.length ? '?' + search.join('&') : '');
  };

  return utils;
});
