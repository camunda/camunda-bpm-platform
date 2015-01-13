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
 * @namespace cam.cockpit
 */
(function(factory) {
  'use strict';
  /*jshint node: true */
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = factory();
  }
  else {
    /* global define: false */
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
  conf.baseUrl = './';

  /**
   * Arguments (query string) used by require.js to build the URL
   * of the different modules to be loaded.
   * This property is used to prevent browsers to keep outdated versions of a file.
   * {@link http://requirejs.org/docs/api.html#config-urlArgs|See the require.js docs for **urlArgs** configuration}
   * @type {string}
   */
  conf.urlArgs = ''/* cache-busting +'bust=' + CACHE_BUSTER /* */;

  //var vendor = 'assets/vendor';
  var vendor = 'bower_components';
  var dojoVendor = 'vendor/dojo';

  /**
   * Keys are module names and values are paths or URLs.
   * {@link http://requirejs.org/docs/api.html#config-paths|See the require.js docs for **paths** configuration}
   * @type {Object.<string, string>}
   */
  conf.paths = {
    'ngDefine':              vendor +'/requirejs-angular-define/src/ngDefine',
    'ngParse':               vendor +'/requirejs-angular-define/src/ngParse',
    'domReady':              vendor +'/requirejs-domready/index',
    'text':                  vendor +'/requirejs-text/text',

    'jquery-mousewheel':     vendor +'/jquery-mousewheel/index',
    'jquery-overscroll':     vendor +'/jquery-overscroll-fixed/index',

    'jquery':                vendor +'/jquery/dist/jquery',
    'bootstrap':             vendor +'/bootstrap/di../bootstrap',
    'bootstrap-part':        vendor +'/bootstrap/js',
    'angular':               vendor +'/angular/angular',
    'angular-resource':      vendor +'/angular-resource/angular-resource',
    'angular-route':         vendor +'/angular-route/angular-route',
    'angular-animate':       vendor +'/angular-animate/angular-animate',
    'angular-sanitize':      vendor +'/angular-sanitize/angular-sanitize',
    'angular-ui':            vendor +'/angular-ui/build/angular-ui',
    'angular-bootstrap':     vendor +'/angular-bootstrap/ui-bootstrap-tpls',
    'jquery-ui':             vendor +'/jquery.ui',

    'angular-data-depend':   vendor +'/angular-data-depend/src/dataDepend',

    'camunda-commons-ui':    './../node_modules/camunda-commons-ui/lib',

    'camunda-cockpit-ui':    'scripts/cockpit',
    'camunda-cockpit':       'scripts/cockpit-bootstrap'
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
                                        ],

    'camunda-cockpit-ui':               [
                                          './services/main',
                                          './pages/main',
                                          './directives/main',
                                          './filters/main',
                                          './resources/main'
                                        ],
    'camunda-cockpit':                  [
      'angular',
      'angular-resource',
      'angular-sanitize',
      'angular-ui',
      'ngDefine',
      // 'bootstrap',
      'jquery-ui/ui/jquery.ui.draggable',
      'camunda-cockpit-ui',
      'domReady!'
    ]

  };

  /**
   * For CommonJS modules (following the CommonJS scaffolding guid lines).
   * {@link http://requirejs.org/docs/api.html#config-packages|See the require.js docs for **packages** configuration}
   * @type {Object.<string, Object>}
   */
  conf.packages = [
    {
      name: 'camunda-commons-ui',
      location: './../node_modules/camunda-commons-ui/lib',
      main: 'index'
    },
    {
      name: 'camunda-commons-ui/util',
      location: './../node_modules/camunda-commons-ui/lib/util',
      main: 'index'
    },
    {
      name: 'bpmn',
      location : './'+ vendor +'/camunda-bpmn.js/src/bpmn',
      main: 'Bpmn'
    },
    {
      name: 'dojo',
      location : './'+ dojoVendor +'/dojo'
    },
    {
      name: 'dojox',
      location : './'+ dojoVendor +'/dojox'
    },
    {
      name: 'services',
      location: './scripts/services',
    },
    {
      name: 'pages',
      location: './scripts/pages',
    },
    {
      name: 'directives',
      location: './scripts/directives',
    },
    {
      name: 'filters',
      location: './scripts/filters',
    },
    {
      name: 'resources',
      location: './scripts/resources',
    },
    {
      name: 'util',
      location: './scripts/util',
      main: 'routeUtil'
    }
  ];


  return conf;
}));
