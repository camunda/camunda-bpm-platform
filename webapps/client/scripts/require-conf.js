'use strict';
/* jshint ignore:start */
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint ignore:end */

define(function() {
  var config = {
    baseUrl: './',

    paths: {
      'text':                       'bower_components/requirejs-text/text',
      'json':                       'bower_components/requirejs-json/json',

      'angular':                    'bower_components/angular/angular',
      'angular-route':              'bower_components/angular-route/angular-route',
      'angular-messages':           'bower_components/angular-messages/angular-messages',
      'angular-sanitize':           'bower_components/angular-sanitize/angular-sanitize',
      'angular-animate':            'bower_components/angular-animate/angular-animate',

      'moment':                     'bower_components/moment/moment',
      'jquery':                     'bower_components/jquery/dist/jquery',
      'bootstrap':                  'bower_components/bootstrap/js',
      'angular-bootstrap':          'bower_components/angular-bootstrap/ui-bootstrap-tpls',
      'angular-moment':             'bower_components/angular-moment/angular-moment',
      'angular-translate':          'bower_components/angular-translate/angular-translate',


      'camunda-tasklist-ui':        'scripts',


      'camunda-commons-ui':         'vendor/camunda-commons-ui/lib',


      'camunda-bpm-sdk':            'vendor/camunda-bpm-sdk-angular',
      'camunda-bpm-sdk-mock':       'vendor/camunda-bpm-sdk-mock',

    },

    shim: {
      'bootstrap':                  ['jquery'],

      'angular':                    {
                                      exports: 'angular',
                                      deps: ['jquery']
                                    },

      'camunda-bpm-sdk':            ['angular'],

      'angular-route':              ['angular'],
      'angular-animate':            ['angular'],

      'angular-bootstrap':          ['angular'],
      'angular-moment':             ['angular', 'moment'],


      'camunda-tasklist-ui':        [
                                      'angular-route',
                                      'angular-animate',

                                      'angular-translate',

                                      'camunda-commons-ui/auth',
                                      'camunda-commons-ui/util/notifications',

                                      'camunda-tasklist-ui/config/routes',
                                      'camunda-tasklist-ui/config/translations',
                                      'camunda-tasklist-ui/config/uris',

                                      'camunda-tasklist-ui/api',
                                      'camunda-tasklist-ui/utils',
                                      'camunda-tasklist-ui/user',
                                      'camunda-tasklist-ui/controls',
                                      'camunda-tasklist-ui/form',
                                      'camunda-tasklist-ui/pile',
                                      'camunda-tasklist-ui/task',
                                      'camunda-tasklist-ui/process',

                                      'bootstrap/collapse',
                                      'camunda-tasklist-ui/navigation/index',

                                      'camunda-commons-ui/directives/notificationsPanel',

                                      'text!camunda-tasklist-ui/index.html',
                                      'json!locales/en.json',
                                      'json!locales/de.json',
                                      'json!locales/fr.json'
                                    ]
    },

    packages: [
      {
        name: 'camunda-commons-ui',
        main: 'index'
      },
      {
        name: 'camunda-commons-ui/auth',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/api',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/controls',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/process',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/form',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/pile',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/task',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/user',
        main: 'index'
      }
    ]
  };

  return config;
});
