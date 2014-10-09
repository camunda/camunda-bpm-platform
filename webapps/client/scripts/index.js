'use strict';

define('snap-win', ['snap-svg'], function(snap) {
  window.Snap = snap;
});

define('camunda-tasklist-ui', [
  'camunda-tasklist-ui/require-conf',
  'camunda-tasklist-ui/utils',
  'snap-win'
], function(
  rjsConf
) {
  /**
   * @namespace cam
   */

  /**
   * @module cam.tasklist
   */

  var tasklistApp;


  var appModules = rjsConf.shim['camunda-tasklist-ui'];


  var deps = [
    'angular',
    'text!camunda-tasklist-ui/index.html'
  ].concat(appModules);



  // converts AMD paths to angular module names
  // "camunda-tasklist-ui/filter" will be "cam.tasklist.filter"
  function rj2ngNames(names) {
    var name, translated = [];
    for (var n = 0; n < names.length; n++) {
      if(names[n].indexOf("!") !== -1) continue;
      name = (require(names[n]) || {}).name;
      if (name) translated.push(name);
    }
    return translated;
  }



  function loaded() {
    var angular = require('angular');
    var $ = angular.element;

    var ngDeps = rj2ngNames(appModules).concat([
      'pascalprecht.translate',
      'ngRoute'
    ]);

    tasklistApp = angular.module('cam.tasklist', ngDeps);

    var notificationsPanel = require('camunda-commons-ui/directives/notificationsPanel');
    tasklistApp.directive('notificationsPanel', notificationsPanel);

    var engineSelect = require('camunda-commons-ui/directives/engineSelect');
    tasklistApp.directive('engineSelect', engineSelect);

    var autoFill = require('camunda-commons-ui/directives/autoFill');
    tasklistApp.directive('autoFill', autoFill);

    tasklistApp.config(require('camunda-tasklist-ui/config/uris'));
    tasklistApp.config(require('camunda-tasklist-ui/config/translations'));
    tasklistApp.config(require('camunda-tasklist-ui/config/routes'));
    tasklistApp.config(require('camunda-tasklist-ui/config/date'));

    tasklistApp.run([
      '$rootScope',
      '$translate',
      'Notifications',
      'camAPI',
    function(
      $rootScope,
      $translate,
      Notifications,
      camAPI
    ) {
      function isAuth() {
        return $rootScope.authentication && $rootScope.authentication.name;
      }


      function checkFilterCreationAccess() {
        if (!isAuth()) {
          throw new Error('Not authenticated');
        }
        var Filter = camAPI.resource('filter');

        $rootScope.authentication.userCanCreateFilter = false;

        Filter.authorizations(function(err, resp) {
          if (err) { throw err; }

          angular.forEach(resp.links, function(link) {
            if (link.rel === 'create') {
              $rootScope.authentication.userCanCreateFilter = true;
            }
          });
        });
      }

      $rootScope.$watch('authentication', function(newValue, oldValue) {
        if (isAuth() && (!oldValue || newValue.name !== oldValue.name)) {
          if (newValue && $rootScope._previousUser !== newValue.name) {
            $rootScope.currentTask = null;
            $rootScope.currentFilter = null;
          }

          $rootScope._previousUser = newValue.name;
          checkFilterCreationAccess();
        }
      });


      $rootScope.$on('authentication.session.expired', function() {
        $translate([
          'SESSION_EXPIRED',
          'SESSION_EXPIRED_MESSAGE'
        ]).then(function(translations) {
          Notifications.addError({
            status: translations.SESSION_EXPIRED,
            message: translations.SESSION_EXPIRED_MESSAGE
          });
        });
      });
    }]);

    $(document).ready(function() {
      angular.bootstrap(document, ['cam.tasklist', 'cam.embedded.forms']);

      setTimeout(function() {
        var $aufocused = $('[autofocus]');
        if ($aufocused.length) {
          $aufocused[0].focus();
        }
      }, 300);
    });
  }


  // configure require.js
  require.config(rjsConf);

  // and load the dependencies
  require(deps, loaded);
});
