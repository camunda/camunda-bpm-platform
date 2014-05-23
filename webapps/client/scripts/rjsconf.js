'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define(function() {
  var config = {
    baseUrl: './',

    paths: {
      // 'domready':                   'bower_components/requirejs-domready/domReady',
      'text':                       'bower_components/requirejs-text/text',

      'angular':                    'bower_components/angular/angular',
      'angular-route':              'bower_components/angular-route/angular-route',
      'angular-messages':           'bower_components/angular-messages/angular-messages',

      'moment':                     'bower_components/moment/moment',
      'jquery':                     'bower_components/jquery/dist/jquery',
      'bootstrap':                  'bower_components/bootstrap/js',
      'angular-bootstrap':          'bower_components/angular-bootstrap/ui-bootstrap-tpls',
      'angular-moment':             'bower_components/angular-moment/angular-moment',


      // 'hyperagent':                 'bower_components/hyperagent/dist/amd',


      'camunda-tasklist':           'scripts',
      // 'camunda-tasklist/controls':  'scripts/controls',
      // 'camunda-tasklist/user':      'scripts/user',
      // 'camunda-tasklist/form':      'scripts/form',
      // 'camunda-tasklist/pile':      'scripts/pile',
      // 'camunda-tasklist/task':      'scripts/task',
      // 'camunda-tasklist/session':   'scripts/session',
      // 'camunda-tasklist/process':   'scripts/process',


      'jquery-mockjax':             'bower_components/jquery-mockjax/jquery.mockjax',
      'camunda-tasklist/mocks':     'scripts/mocks',
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

      'angular-bootstrap':          ['angular'],
      'angular-moment':             ['angular', 'moment'],

      // 'hyperagent':                 {
      //                                 exports: 'Hyperagent',
      //                                 deps: [],
      //                               },
      // 'camunda-tasklist/pile/data': ['hyperagent'],
      // 'camunda-tasklist/task/data': ['hyperagent'],


      'camunda-tasklist':           [
                                      'angular-route',


                                      'camunda-tasklist/utils',
                                      'camunda-tasklist/user',
                                      'camunda-tasklist/controls',
                                      'camunda-tasklist/form',
                                      'camunda-tasklist/pile',
                                      'camunda-tasklist/task',
                                      'camunda-tasklist/process',
                                      'camunda-tasklist/session',

                                      'bootstrap/collapse',
                                      'camunda-tasklist/navigation/index',
                                      'camunda-tasklist/notifier/index',

                                      'text!camunda-tasklist/index.html'
                                    ],

      'jquery-mockjax':             ['jquery'],
      'underscore.string':          ['underscore'],
      'fixturer':                   ['underscore.string'],
      'camunda-tasklist/mocks':     [
                                      'uuid',
                                      'fixturer',
                                      'angular',
                                      'jquery',
                                      'jquery-mockjax'
                                    ]
    },

    packages: [
      // {
      //   name: 'hyperagent',
      //   main: 'hyperagent'
      // },

      {
        name: 'camunda-tasklist',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/controls',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/process',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/session',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/form',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/pile',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/task',
        main: 'index'
      },
      {
        name: 'camunda-tasklist/user',
        main: 'index'
      },

      {
        name: 'camunda-tasklist/mocks',
        main: 'index'
      }
    ]
  };

  return config;
});
