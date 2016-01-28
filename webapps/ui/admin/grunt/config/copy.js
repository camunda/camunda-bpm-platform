'use strict';

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;

function distFileProcessing(content, srcpath) {
  // removes the template comments
  content = content
            .split('\n').filter(function(line) {
              return !commentLineExp.test(line);
            }).join('\n');

  // var date = new Date();
  // var cacheBuster = [date.getFullYear(), date.getMonth(), date.getDate()].join('-');
  // content = content
  //           .replace(/\/\* cache-busting /, '/* cache-busting */')
  //           .replace(/CACHE_BUSTER/g, requireConfExp.test(srcpath) ? '\''+ cacheBuster +'\'' : cacheBuster);

  return content;
}

function devFileProcessing(content, srcpath) {
  /* jshint validthis: true */
  var liveReloadPort = this.config.get('pkg.gruntConfig.livereloadPort');
  /* jshint validthis: false */

  content = content
            .replace(/\/\* live-reload/, '/* live-reload */')
            .replace(/LIVERELOAD_PORT/g, liveReloadPort);

  // content = content
  //           .replace(/\/\* cache-busting/, '/* cache-busting */')
  //           .replace(/CACHE_BUSTER/g, (new Date()).getTime());

  return content;
}

module.exports = function(config, copyConf) {
  var grunt = config.grunt;

  copyConf.admin_index = {
    options: {
      process: function() {
        return devFileProcessing.apply(grunt, arguments);
      }
    },
    files: [
      {
        expand: true,
        cwd: '<%= pkg.gruntConfig.adminSourceDir %>/scripts/',
        src: [
          'index.html',
          'camunda-admin-bootstrap.js'
        ],
        dest: '<%= pkg.gruntConfig.adminBuildTarget %>/'
      }
    ]
  };

  copyConf.admin_dist = {
      options: {
        process: function() {
          return distFileProcessing.apply(grunt, arguments);
        }
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.adminSourceDir %>/scripts/WEB-INF',
          src: ['*'],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/WEB-INF'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.adminSourceDir %>/scripts/',
          src: [
            'index.html',
            'camunda-admin-bootstrap.js'
          ],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/'
        }
      ]
    };

    copyConf.admin_assets = {
      files: [
        // custom styles and/or other css files
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.adminSourceDir %>/styles',
          src: ['*.css'],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/styles/'
        },

        // images, fonts & stuff
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.adminSourceDir %>/',
          src:  [
            '{fonts,images}/**/*.*'
          ],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/assets'
        },

        // bootstrap fonts
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/node_modules/bootstrap/fonts',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/fonts/'
        },
        // open sans fonts
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/vendor/fonts',
          src: ['*.{eot,svg,ttf,woff,woff2}'],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/fonts/'
        },

        // placeholder shims
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/vendor',
          src: ['placeholders.*'],
          dest: '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/'
        }
      ]
    };
};
