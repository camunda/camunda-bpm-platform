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
      'angular-sanitize':           'bower_components/angular-sanitize/angular-sanitize',
      'angular-animate':            'bower_components/angular-animate/angular-animate',
      'angular-data-depend':        'bower_components/angular-data-depend/src/dataDepend',

      'moment':                     'bower_components/moment/moment',
      'jquery':                     'bower_components/jquery/dist/jquery',
      'bootstrap':                  'bower_components/bootstrap/js',
      'angular-bootstrap':          'bower_components/angular-bootstrap/ui-bootstrap-tpls',
      'angular-moment':             'bower_components/angular-moment/angular-moment',
      'angular-translate':          'bower_components/angular-translate/angular-translate',

      'bpmn-js':                    'bower_components/bpmn-js/bpmn-viewer',

      'lodash':                     'bower_components/lodash/dist/lodash',
      'sax':                        'bower_components/sax/lib/sax',
      'snap-svg':                   'bower_components/Snap.svg/dist/snap.svg-min',

      'camunda-tasklist-ui':        'scripts',


      'camunda-commons-ui':         'vendor/camunda-commons-ui/lib',


      'camunda-bpm-sdk':            'bower_components/camunda-bpm-sdk-js/camunda-bpm-sdk-angular',

      'placeholders-js':            'bower_components/Placeholders.js/lib'
    },

    shim: {
      'bootstrap':                  ['jquery'],

      'angular':                    {
                                      exports: 'angular',
                                      deps: ['jquery']
                                    },

      // 'snap-svg':                   {exports: 'Snap'},

      'camunda-bpm-sdk':            ['angular'],

      'angular-route':              ['angular'],
      'angular-animate':            ['angular'],

      'angular-data-depend':        ['angular'],

      'angular-bootstrap':          ['angular'],
      'angular-moment':             ['angular', 'moment'],

      'bpmn-js':                    [
                                      'lodash',
                                      'snap-svg',
                                      'jquery',
                                      'sax'
                                    ],

      'placeholders-js':            [
                                    ],

      'camunda-tasklist-ui':        [
                                      'angular-route',
                                      'angular-animate',

                                      'angular-translate',
                                      'angular-data-depend',

                                      'camunda-commons-ui/auth',
                                      'camunda-commons-ui/util/notifications',
                                      'camunda-commons-ui/filter/date/index',
                                      'camunda-commons-ui/plugin/index',
                                      'camunda-commons-ui/search/index',
                                      'camunda-commons-ui/services/index',
                                      'camunda-commons-ui/util/index',

                                      'camunda-tasklist-ui/config/date',
                                      'camunda-tasklist-ui/config/routes',
                                      'camunda-tasklist-ui/config/locales',
                                      'camunda-tasklist-ui/config/tooltip',
                                      'camunda-tasklist-ui/config/uris',

                                      'camunda-tasklist-ui/api',
                                      'camunda-tasklist-ui/user',
                                      'camunda-tasklist-ui/filter',
                                      'camunda-tasklist-ui/tasklist',
                                      'camunda-tasklist-ui/variable',
                                      'camunda-tasklist-ui/task',
                                      'camunda-tasklist-ui/process',
                                      'camunda-tasklist-ui/controller/cam-tasklist-app-ctrl',
                                      'camunda-tasklist-ui/controller/cam-tasklist-view-ctrl',
                                      'camunda-tasklist-ui/form',
                                      'camunda-tasklist-ui/services/cam-tasklist-assign-notification',
                                      'camunda-tasklist-ui/services/cam-tasklist-configuration',

                                      'bootstrap/collapse',
                                      'camunda-tasklist-ui/navigation/index',

                                      'camunda-commons-ui/directives/notificationsPanel',
                                      'camunda-commons-ui/directives/engineSelect',
                                      'camunda-commons-ui/directives/autoFill',
                                      'camunda-commons-ui/directives/nl2br',
                                      'camunda-commons-ui/directives/compileTemplate',
                                      'camunda-commons-ui/widgets/inline-field/cam-widget-inline-field',

                                      'placeholders-js/utils',
                                      'placeholders-js/main',
                                      'placeholders-js/adapters/placeholders.jquery',

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
        name: 'camunda-tasklist-ui/process',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/filter',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/tasklist',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/task',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/variable',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/user',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/widgets',
        main: 'index'
      },
      {
        name: 'camunda-tasklist-ui/form',
        main: 'index'
      }
    ]
  };

  return config;
});
