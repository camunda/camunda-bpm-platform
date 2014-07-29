'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define(function() {
  var config = {
    baseUrl: './',

    paths: {
      'text':                       'bower_components/requirejs-text/text',

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


      'camunda-tasklist-ui':        'scripts',


      'camunda-commons-ui':         'vendor/camunda-commons-ui/lib',


      'camunda-bpm-forms':          'vendor/camunda-embedded-forms',
      'camunda-bpm-sdk':            'vendor/camunda-bpm-sdk',
      'camunda-bpm-sdk-mock':       'vendor/camunda-bpm-sdk-mock'
    },

    shim: {
      'bootstrap':                  ['jquery'],

      'angular':                    {
                                      exports: 'angular',
                                      deps: ['jquery']
                                    },
      'angular-route':              ['angular'],
      'angular-animate':            ['angular'],

      'angular-bootstrap':          ['angular'],
      'angular-moment':             ['angular', 'moment'],


      'camunda-tasklist-ui':        [
                                      'angular-route',
                                      'angular-animate',

                                      'camunda-commons-ui/auth',
                                      'camunda-commons-ui/util/notifications',

                                      'camunda-tasklist-ui/api',
                                      'camunda-tasklist-ui/utils',
                                      'camunda-tasklist-ui/user',
                                      'camunda-tasklist-ui/controls',
                                      'camunda-tasklist-ui/form',
                                      'camunda-tasklist-ui/pile',
                                      'camunda-tasklist-ui/task',
                                      'camunda-tasklist-ui/process',
                                      'camunda-tasklist-ui/session',

                                      'bootstrap/collapse',
                                      'camunda-tasklist-ui/navigation/index',
                                      'camunda-tasklist-ui/notifier/index',

                                      'text!camunda-tasklist-ui/index.html'
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
        name: 'camunda-tasklist-ui/session',
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
