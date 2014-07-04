/**
 * @namespace angular
 * @description Angular.js {@link http://docs.angularjs.org/api|documentation won't help much}
 */

/**
 * @name Module
 * @memberof angular
 * @description {@link http://docs.angularjs.org/api/angular.Module|The angular.js Module class}
 */

/**
 * @namespace cam
 */

/**
 * @namespace cam.common
 */
(function(factory) {
  'use strict';
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = factory();
  }
  else {
    define([], factory);
  }
}(function() {
  'use strict';
  /**
   * A UMD module that provides some configuration for
   * {@link http://requirejs.org/docs/api.html#config|require.js}.
   *
   * @exports require-conf
   *
   * @example
   *    require(['../../require-conf'], function(rjsConf) {
   *
   *      // configure require.js...
   *      require(rjsConf);
   *
   *      // ...start loading dependencies...
   *      require([
   *        'backbone',
   *        'whatever'
   *      ], function(Backbone, Whatever) {
   *        // ...to do something
   *      });
   *    });
   */
  var conf = {};

  /**
   * The base path/URL used by require.js to build the URL
   * of the different modules to be loaded.
   * {@link http://requirejs.org/docs/api.html#config-baseUrl|See the require.js docs for **baseUrl** configuration}
   * @type {string}
   */
  conf.baseUrl = '';

  /**
   * Arguments (query string) used by require.js to build the URL
   * of the different modules to be loaded.
   * This property is used to prevent browsers to keep outdated versions of a file.
   * {@link http://requirejs.org/docs/api.html#config-urlArgs|See the require.js docs for **urlArgs** configuration}
   * @type {string}
   */
  conf.urlArgs = ''/* cache-busting +'bust=' + CACHE_BUSTER /* */;

  /**
   * Keys are module names and values are paths or URLs.
   * {@link http://requirejs.org/docs/api.html#config-paths|See the require.js docs for **paths** configuration}
   * @type {Object.<string, string>}
   */
  conf.paths = {
    'ngDefine':              'bower_components/requirejs-angular-define/src/ngDefine',
    'ngParse':               'bower_components/requirejs-angular-define/src/ngParse',
    'domReady':              'bower_components/requirejs-domready/index',
    'jquery-mousewheel':     'bower_components/jquery-mousewheel/index',
    'jquery-overscroll':     'bower_components/jquery-overscroll-fixed/index',

    'jquery':                'bower_components/jquery/dist/jquery',
    'bootstrap':             'bower_components/bootstrap/di../bootstrap',
    'bootstrap-part':        'bower_components/bootstrap/js',
    'angular':               'bower_components/angular/angular',
    'angular-resource':      'bower_components/angular-resource/angular-resource',
    'angular-route':         'bower_components/angular-route/angular-route',
    'angular-animate':       'bower_components/angular-animate/angular-animate',
    'angular-sanitize':      'bower_components/angular-sanitize/angular-sanitize',
    'angular-ui':            'bower_components/angular-ui/build/angular-ui',
    'angular-bootstrap':     'bower_components/angular-bootstrap/ui-bootstrap-tpls',
    'jquery-ui':             'bower_components/jquery.ui',

    'angular-data-depend':   'bower_components/angular-data-depend/src/dataDepend',

    'camunda-commons-ui':    'bower_components/camunda-commons-ui/lib'
  };

  /**
   * Keys are module names and values are information on how to shim the modules.
   * {@link http://requirejs.org/docs/api.html#config-shim|See the require.js docs for **shim** configuration}
   * @type {Object.<string, (Object|array)>}
   */
  conf.shim = {
    'jquery-mousewheel' :               ['jquery'],
    'jquery-overscroll' :               ['jquery'],
    'jquery-ui-core' :                  ['jquery'],
    'bootstrap' :                       ['jquery'],
    'bootstrap-part' :                  ['jquery'],
    'bootstrap-part/transition' :       ['jquery'],
    'bootstrap-part/collapse' :         ['jquery'],
    'angular' :                         {
                                          deps: [
                                            'jquery',
                                            // needed to ensure responsive navigation
                                            'bootstrap-part/transition',
                                            'bootstrap-part/collapse'
                                          ],
                                          exports: 'angular'
                                        },
    'angular-resource':                 ['angular'],
    'angular-sanitize':                 ['angular'],
    'angular-route':                    ['angular'],
    'angular-animate':                  ['angular'],
    'angular-bootstrap':                ['angular'],
    'angular-ui':                       [
                                          'angular-bootstrap'
                                        ],

    'jquery-ui/ui/jquery.ui.widget':    ['jquery-ui/ui/jquery.ui.core'],
    'jquery-ui/ui/jquery.ui.mouse':     [
                                          'jquery-ui/ui/jquery.ui.widget'
                                        ],
    'jquery-ui/ui/jquery.ui.draggable': [
                                          'jquery-ui/ui/jquery.ui.widget',
                                          'jquery-ui/ui/jquery.ui.mouse'
                                        ]
  };

  /**
   * For CommonJS modules (following the CommonJS scaffolding guid lines).
   * {@link http://requirejs.org/docs/api.html#config-packages|See the require.js docs for **packages** configuration}
   * @type {Object.<string, Object>}
   */
  conf.packages = [
    {
      name: 'cockpit',
      location: '.',
      main: 'cockpit'
    },
    {
      name: 'bpmn',
      location : './bower_components/camunda-bpmn.js/src/bpmn'
    },
    {
      name: 'dojo',
      location : './vendor/dojo'
    },
    {
      name: 'dojox',
      location : './vendor/dojox'
    }
  ];


  /**
   * A set of utilities to bootstrap applications
   */
  conf.utils = {};

  /**
   * Utility to ensure compatibility of test scenarios loaded with require.js
   * {@link http://stackoverflow.com/questions/15499997/how-to-use-angular-scenario-with-requirejs}
   *
   * @param {string} appName - The "angular app name"
   */
  conf.utils.ensureScenarioCompatibility = function (appName) {
    var html = document.getElementsByTagName('html')[0];

    html.setAttribute('ng-app', appName);
    if (html.dataset) {
      html.dataset.ngApp = appName;
    }

    if (top !== window) {
      window.parent.postMessage({ type: 'loadamd' }, '*');
    }
  };

  /**
   * Utility function to bootsrap angular applications
   *
   * @param angular {Object} - angular, obviously
   * @param appName {string} - the package name of the application
   */
  conf.utils.bootAngular = function(angular, appName) {
    angular.bootstrap(document, [ appName ]);

    conf.utils.ensureScenarioCompatibility(appName);
  };

  /* live-reload
  // loads livereload client library (without breaking other scripts execution)
  require(['jquery'], function($) {
    $('body').append('<script src="//localhost:LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
  });
  /* */

  return conf;
}));
