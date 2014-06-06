/* global ngDefine: false */

ngDefine('cockpit.services.page', ['angular'], function(module, angular) {
  'use strict';

  /**
   * A service to manage a page page.
   *
   * @name page
   * @memberof cam.cockpit.services
   * @type angular.service
   *
   * @module cam.cockpit.services.page
   */
  module.service('page', [
    '$rootScope',
  function(
    $rootScope
  ) {

    var page = {
      title: 'camunda',
      breadcrumbs: []
    };

    var headTitle = angular.element('head title');

    // add a listener to the $rootScope to propagate the changes in the page title
    // sure... we could put that in the titleSet() function
    // or elsewhere but it's almost philosophical
    $rootScope.$on('page.title.changed', function() {
      headTitle.text(page.title);
    });

    return {
      /**
       * Set the document/page title
       *
       * @param {sring} newTitle          - the new document/page title
       *
       * @returns {angular.Service} this  - the service
       */
      titleSet: function(newTitle) {
        page.title = newTitle;
        // $rootScope.$broadcast('page.changed', 'title', page.title);
        $rootScope.$broadcast('page.title.changed', page.title);
        return this;
      },

      /**
       * Get the document/page title
       *
       * @returns {string} - the document/page title
       */
      titleGet: function() {
        return page.title;
      },

      /**
       * Adds one or more page
       *
       * @param {(Object.<string, *>|Object[])} crumb - a breadcrumb object or an array of breadcrumb objects
       *
       * @returns {angular.Service} this  - the service
       */
      breadcrumbsAdd: function(crumb) {
        if (angular.isArray(crumb)) {
          return angular.forEach(crumb, this.breadcrumbsAdd);
        }

        if (angular.isFunction(crumb)) {
          var callback = crumb;
          crumb = {
            callback: callback
          };
        }

        crumb.label = crumb.label || '…';

        page.breadcrumbs.push(crumb);

        $rootScope.$broadcast('page.breadcrumbs.changed', page.breadcrumbs);
        return this;
      },

      /**
       * Insert a link (or array of links) at a given index
       *
       * @param {integer} index          - the position at which the new
       *                                   element(s) will be insert
       *
       * @param {(Object|Array)} crumb   - the new element(s) to insert
       *
       * @return {angular.Service} this  - the service
       */
      breadcrumbsInsertAt: function(index, crumb) {
        page.breadcrumbs = page.breadcrumbs.slice(0, index)
          .concat(angular.isArray(crumb) ? crumb : [crumb])
          .concat(page.breadcrumbs.slice(index + 1));

        $rootScope.$broadcast('page.breadcrumbs.changed', page.breadcrumbs);
        return this;
      },

      /**
       * Get the page breadcrumbs
       *
       * @param {(integer|string)} index  - can be an integer
       *                                    or "last" (as shortcut for breadcrumbs[breadcrumbs.length - 1])
       *
       * @returns {(Array|Object)}        - an array of breadcrumb objects
       *                                    or 1 breadcrumb object when the index is given
       */
      breadcrumbsGet: function(index) {
        if (index) {
          if (index === 'last') {
            index = page.length - 1;
          }
          return page.breadcrumbs[index];
        }
        return page.breadcrumbs;
      },

      /**
       * Clears the breadcrumb (remove all items)
       *
       * @returns {angular.Service} this  - the service
       */
      breadcrumbsClear: function() {
        page.breadcrumbs = [];

        $rootScope.$broadcast('page.breadcrumbs.changed', page.breadcrumbs);
        return this;
      }
    };
  }]);
});
