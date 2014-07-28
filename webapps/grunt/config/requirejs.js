module.exports = function(config) {
  'use strict';
  var _ = require('underscore');
  var path = require('path');
  var rjsConfPath = path.resolve('./client/scripts/require-conf');
  var rjsConf = require(rjsConfPath);

  var deps = [
    'camunda-tasklist-ui/require-conf',
    './../node_modules/requirejs/require',
    // 'camunda-commons-ui/util',
    'camunda-commons-ui/auth',
    'jquery',
    'angular',
    'moment',

    'camunda-bpm-forms',
    'camunda-bpm-sdk',

    'angular-bootstrap',
    'angular-route',
    'angular-animate',
    'angular-moment'
  ];//.concat(rjsConf.shim['camunda-tasklist-ui']);

  _.extend(rjsConf.paths, {
    'camunda-commons-ui': './../node_modules/camunda-commons-ui/lib',
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
        config.grunt.verbose.writeln('onModuleBundleComplete', data.path+':\n\n'+data.included.join('\n') +'\n');

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
        include: deps
      }
    },

    scripts: {
      options: {
        name: 'camunda-tasklist-ui',
        out: '<%= buildTarget %>/scripts/<%= pkg.name %>.js',
        exclude: deps,
        include: rjsConf.shim['camunda-tasklist-ui']
      }
    }
  };

  return rConf;
};
