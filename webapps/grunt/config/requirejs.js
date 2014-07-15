module.exports = function(config) {
  'use strict';
  var _ = require('underscore');
  var fs = require('fs');
  var path = require('path');
  var grunt = config.grunt;
  var rjsConfPath = path.resolve('./client/scripts/require-conf');
  var rjsConf = require(rjsConfPath);

  var deps = [
    'camunda-cockpit-ui/require-conf',
    './../node_modules/requirejs/require',
    'jquery',
    'angular',
    'moment',
    'angular-bootstrap',
    'angular-route',
    'angular-animate',
    'angular-moment'
  ];

  _.extend(rjsConf.paths, {
    'require-conf': 'scripts/require-conf'
  });

  var rConf = {
    options: {
      stubModules: ['text'],

      optimize: '<%= (buildTarget === "dist" ? "uglify2" : "none") %>',
      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: './<%= pkg.gruntConfig.clientDir %>',
      // baseUrl: config.clientDir,

      paths: rjsConf.paths,
      shim: rjsConf.shim,
      packages: rjsConf.packages,

      onModuleBundleComplete: function (data) {
        /*
        data.name: the bundle name.
        data.path: the bundle path relative to the output directory.
        data.included: an array of items included in the build bundle.
        If a file path, it is relative to the output directory. Loader
        plugin IDs are also included in this array, but depending
        on the plugin, may or may not have something inlined in the
        module bundle.
        */
        console.info('onModuleBundleComplete', data.path+':\n\n'+data.included.join('\n') +'\n');

        // // add a timestamp to the sourcemap URL to prevent caching
        // fs.readFile(data.path, {encoding: 'utf8'}, function(err, content) {
        //   // console.info('onModuleBundleComplete', data.name, content);
        //   content = content + '?' + (new Date()).getTime();
        //   fs.writeFileSync(data.path, content);
        // });
      }
    },


    dependencies: {
      options: {
        create: true,
        name: '<%= pkg.name %>-deps',
        out: '<%= buildTarget %>/scripts/deps.js',
        include: deps.concat([
          'camunda-cockpit-ui/require-conf'
        ])
      }
    },

    scripts: {
      options: {
        name: 'camunda-cockpit-ui',
        out: '<%= buildTarget %>/scripts/<%= pkg.name %>.js',
        exclude: deps.concat([
          'camunda-cockpit-ui/require-conf'
        ]),
        include: rjsConf.shim['camunda-cockpit-ui']
      }
    }
  };

  return rConf;
};
