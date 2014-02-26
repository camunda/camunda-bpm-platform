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
    'ProcessInstanceResource',
  function(
    $rootScope,
    ProcessInstanceResource
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

        // that sucks - badly -
        // to be really relevant the concepts of
        // process/instance should be avoided in this file
        var processDefinition = crumb.processDefinition;
        switch (crumb.type) {
          case 'processDefinition':
            crumb.label = crumb.label || processDefinition.name || processDefinition.key || processDefinition.id;
            crumb.href = crumb.href || '#/process-definition/' + processDefinition.id;
            crumb.divider = '/';

            page.breadcrumbs.push(crumb);
            break;

          case 'processInstance':
            var processInstance = crumb.processInstance;

            crumb.label = crumb.label || processInstance.id;
            crumb.href = crumb.href || '#/process-instance/' + processInstance.id;
            crumb.divider = ':';

            ProcessInstanceResource.count({ subProcessInstance: processInstance.id }).$then(function(response) {
              var count = response.data.count;

              if (count === 1) {
                page.breadcrumbs.unshift({
                  type: 'expand',
                  divider: '/',
                  processInstance: processInstance
                });
              }
            });

            page.breadcrumbs.push(crumb);
            break;
        }

        // $rootScope.$broadcast('page.changed', 'breadcrumbs', page.breadcrumbs);
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
        // $rootScope.$broadcast('page.changed', 'breadcrumbs', page.breadcrumbs);
        $rootScope.$broadcast('page.breadcrumbs.changed', page.breadcrumbs);
        return this;
      }
    };
  }]);
});
