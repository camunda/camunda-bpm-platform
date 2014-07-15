'use strict';

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var requireConfExp =  /require-conf.js$/;

function distFileProcessing(content, srcpath) {
  // removes the template comments
  content = content
            .split('\n').filter(function(line) {
              return !commentLineExp.test(line);
            }).join('\n');

  var date = new Date();
  var cacheBuster = [date.getFullYear(), date.getMonth(), date.getDate()].join('-');
  content = content
            .replace(/\/\* cache-busting /, '/* cache-busting */')
            .replace(/CACHE_BUSTER/g, requireConfExp.test(srcpath) ? '\''+ cacheBuster +'\'' : cacheBuster);

  return content;
}

function devFileProcessing(content, srcpath) {
  /* jshint validthis: true */
  var liveReloadPort = this.config.get('app.liveReloadPort');
  /* jshint validthis: false */

  if (requireConfExp.test(srcpath)) {
    content = content
              .replace(/\/\* live-reload/, '/* live-reload */')
              .replace(/LIVERELOAD_PORT/g, liveReloadPort);
  }

  content = content
            .replace(/\/\* cache-busting/, '/* cache-busting */')
            .replace(/CACHE_BUSTER/g, (new Date()).getTime());

  return content;
}

module.exports = function(config) {
  var grunt = config.grunt;

  return {
    development: {
      options: {
        process: function() {
          devFileProcessing.apply(grunt, arguments);
        }
      },
      files: [
        // {
        //   expand: true,
        //   cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/WEB-INF',
        //   src: ['*'],
        //   dest: '<%= buildTarget %>/WEB-INF'
        // },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            'require-conf.js',
            'index.html',
            '{app,plugin,develop,common}/**/*.{js,html}'
          ],
          dest: '<%= buildTarget %>/'
        },
        // {
        //   expand: true,
        //   cwd: 'node_modules/camunda-tasklist-ui/dist/',
        //   src: ['**'],
        //   dest: '<%= buildTarget %>/app/tasklist/default/'
        // },
        // {
        //   expand: true,
        //   cwd: 'node_modules/camunda-tasklist-ui/dist/',
        //   src: ['index.html'],
        //   dest: '<%= buildTarget %>/app/tasklist/'
        // },
        {
          expand: true,
          cwd: 'node_modules/camunda-tasklist-ui/dist/',
          src: ['**'],
          dest: '<%= buildTarget %>/app/tasklist/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            'assets/fonts/**/*.{css,eot,svg,ttf,woff}'
          ],
          dest: '<%= buildTarget %>/'
        }
      ]
    },

    dist: {
      options: {
        process: function() {
          distFileProcessing.apply(grunt, arguments);
        }
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/WEB-INF',
          src: ['*'],
          dest: '<%= buildTarget %>/WEB-INF'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            'require-conf.js',
            'index.html'
          ],
          dest: '<%= buildTarget %>/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            '{app,plugin,develop,common}/**/*.{js,html}'
          ],
          dest: '<%= buildTarget %>/'
        }
      ]
    },

    assets: {
      process: function(content, srcpath) {
        grunt.log.ok('Copy '+ srcpath);
        return content;
      },
      files: [
        // images, fonts & stuff
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/',
          src:  [
            '{fonts,images}/**/*.*'
          ],
          dest: '<%= buildTarget %>/assets'
        },

        // dojo & dojox
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/vendor/dojo',
          src:  [
            '**/*.*'
          ],
          dest: '<%= buildTarget %>/assets/vendor'
        },

        // requirejs
        {
          // src: '<%= pkg.gruntConfig.clientDir %>/scripts/assets/vendor/requirejs/index.js',
          // dest: '<%= buildTarget %>/assets/vendor/requirejs/require.js'
          src: '<%= pkg.gruntConfig.clientDir %>/bower_components/requirejs/index.js',
          dest: '<%= buildTarget %>/assets/vendor/requirejs/require.js'
        },

        // others
        {
          expand: true,
          // cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/assets',
          cwd: '<%= pkg.gruntConfig.clientDir %>/bower_components',
          src: [
            // '!vendor/requirejs/**/*',
            // 'css/**/*',
            // 'img/**/*',
            // 'vendor/**/*.{js,css,jpg,png,gif,html,eot,ttf,svg,woff,htc}'
            '!requirejs/**/*',
            '**/*.{js,css,jpg,png,gif,html,eot,ttf,svg,woff,htc}'
          ],
          dest: '<%= buildTarget %>/assets/vendor'
        }
      ]
    },

    sdk: {
      files: [
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-embedded-forms.js',
          dest: '<%= pkg.gruntConfig.clientDir %>/bower_components/camunda-bpm-form/index.js'
        },
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk.js',
          dest: '<%= pkg.gruntConfig.clientDir %>/bower_components/camunda-bpm-sdk-js/index.js'
        },
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk-mock.js',
          dest: '<%= pkg.gruntConfig.clientDir %>/bower_components/camunda-bpm-sdk-js-mock/index.js'
        }
      ]
    }
  };
};


module.exports.devFileProcessing = devFileProcessing;
module.exports.distFileProcessing = distFileProcessing;
