'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

  /**
   * A service to manage a page page.
   *
   * @name page
   * @memberof cam.cockpit.services
   * @type angular.service
   *
   * @module cam.cockpit.services.page
   */

module.exports = [
  '$rootScope',
  '$location',
  'camAPI',
  function(
    $rootScope,
    $location,
    camAPI
  ) {

    var page = {
      title: 'Camunda',
      breadcrumbs: []
    };

    var headTitle = angular.element(document.querySelector('head title'));
    var originalTitle = headTitle[0].textContent || 'Camunda Cockpit';

    // add a listener to the $rootScope to propagate the changes in the page title
    // sure... we could put that in the titleSet() function
    // or elsewhere but it's almost philosophical
    $rootScope.$on('page.title.changed', function() {
      headTitle.text([originalTitle, page.title].join(' | '));
    });



    $rootScope.isActivePage = function(pageName) {
      return $location.path().indexOf('/' + pageName) === 0 ? 'active' : '';
    };


    function getUserProfile(auth) {
      if (!auth || !auth.name) {
        $rootScope.userFullName = null;
        return;
      }

      var userService = camAPI.resource('user');
      userService.profile(auth.name, function(err, info) {
        if (err) {
          $rootScope.userFullName = null;
          throw err;
        }
        $rootScope.userFullName = info.firstName + ' ' + info.lastName;
      });
    }
    $rootScope.$on('authentication.changed', function(ev, auth) {
      getUserProfile(auth);
    });
    getUserProfile($rootScope.authentication);

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
  }];
