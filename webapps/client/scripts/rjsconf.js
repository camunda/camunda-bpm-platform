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
      'angular-animate':            'bower_components/angular-animate/angular-animate',

      'moment':                     'bower_components/moment/moment',
      'jquery':                     'bower_components/jquery/dist/jquery',
      'bootstrap':                  'bower_components/bootstrap/js',
      'angular-bootstrap':          'bower_components/angular-bootstrap/ui-bootstrap-tpls',
      'angular-moment':             'bower_components/angular-moment/angular-moment',


      // 'hyperagent':                 'bower_components/hyperagent/dist/amd',


      'camunda-tasklist-ui':        'scripts',


      'jquery-mockjax':             'bower_components/jquery-mockjax/jquery.mockjax',
      'camunda-tasklist-ui-mocks':  'scripts/mocks',
      'uuid':                       'bower_components/node-uuid/uuid',
      'fixturer':                   'bower_components/fixturer/index',
      'underscore':                 'bower_components/underscore/index',
      'underscore.string':          'bower_components/underscore.string/index'
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

      // 'hyperagent':                 {
      //                                 exports: 'Hyperagent',
      //                                 deps: [],
      //                               },
      // 'camunda-tasklist-ui/pile/data': ['hyperagent'],
      // 'camunda-tasklist-ui/task/data': ['hyperagent'],


      'camunda-tasklist-ui':        [
                                      'angular-route',
                                      'angular-animate',


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
                                    ],

      'jquery-mockjax':             ['jquery'],
      'underscore.string':          ['underscore'],
      'fixturer':                   ['underscore.string'],
      'camunda-tasklist-ui-mocks':  [
                                      'uuid',
                                      'fixturer',
                                      'angular',
                                      'jquery',
                                      'jquery-mockjax'
                                    ]
    },

    packages: [
      {
        name: 'camunda-tasklist-ui',
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
      },

      // NOTE: not "/mocks" but "-mocks"!
      {
        name: 'camunda-tasklist-ui-mocks',
        main: 'index'
      }
    ]
  };

  return config;
});
