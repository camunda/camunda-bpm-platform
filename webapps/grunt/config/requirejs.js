module.exports = function() {
  'use strict';
  var commons = require('camunda-commons-ui');
  var _ = commons.utils._;
  var rjsConf = commons.requirejs({
    // pathPrefix: '../node_modules/camunda-commons-ui'
  });

  var deps = [
    'requirejs',
    'camunda-commons-ui',
    'angular-resource',
    'angular-sanitize',
    'angular-route',
    'angular-bootstrap'
  ];

  var rConf = {
    options: {
      stubModules: [
        'json',
        'text'
      ],

      optimize: '<%= (buildTarget === "dist" ? "uglify2" : "none") %>',
      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: './<%= pkg.gruntConfig.clientDir %>',

      paths: _.extend(rjsConf.paths, {
        'camunda-tasklist-ui': 'scripts/camunda-tasklist-ui',
      }),

      shim: _.extend(rjsConf.shim, {}),

      packages: rjsConf.packages.concat([
        {
          name: 'camunda-commons-ui/auth',
          main: 'index'
        },
        {
          name: 'api',
          main: 'index'
        },
        {
          name: 'process',
          main: 'index'
        },
        {
          name: 'filter',
          main: 'index'
        },
        {
          name: 'tasklist',
          main: 'index'
        },
        {
          name: 'task',
          main: 'index'
        },
        {
          name: 'variable',
          main: 'index'
        },
        {
          name: 'user',
          main: 'index'
        },
        {
          name: 'widgets',
          main: 'index'
        },
        {
          name: 'form',
          main: 'index'
        }
      ])
    },


    dependencies: {
      options: {
        create: true,
        name: '<%= pkg.name %>-deps',
        out: '<%= buildTarget %>/scripts/deps.js',
        include: deps
      }
    },

    scripts: {
      options: {
        name: 'camunda-tasklist-ui',
        out: '<%= buildTarget %>/scripts/<%= pkg.name %>.js',
        exclude: deps,
        include: [
          'scripts/config/date',
          'scripts/config/routes',
          'scripts/config/locales',
          'scripts/config/tooltip',
          'scripts/config/uris',

          'scripts/controller/cam-tasklist-app-ctrl',
          'scripts/controller/cam-tasklist-view-ctrl',
          'scripts/services/cam-tasklist-assign-notification',
          'scripts/services/cam-tasklist-configuration',

          'scripts/user/index',
          'scripts/variable/index',
          'scripts/tasklist/index',
          'scripts/task/index',
          'scripts/process/index',
          'scripts/navigation/index',
          'scripts/form/index',
          'scripts/filter/index',
          'scripts/api/index'
        ]
      }
    }
  };

  return rConf;
};
