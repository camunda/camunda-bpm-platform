module.exports = {
  webapp_libs : {
      files: [
        { expand: true, cwd: 'node_modules/requirejs/', src: ['require.js'], dest: '<%= pkg.gruntConfig.libTargetDir %>/' },
        { expand: true, cwd: 'node_modules/requirejs-angular-define/dist/', src: ['ngDefine.js'], dest: '<%= pkg.gruntConfig.libTargetDir %>/' },
        { expand: true, cwd: 'node_modules/camunda-commons-ui/cache/', src: ['deps.js'], dest: '<%= pkg.gruntConfig.libTargetDir %>/' },
        // { expand: true, cwd: 'node_modules/angular/', src: ['angular.js'], dest: '<%= pkg.gruntConfig.libTargetDir %>/' },
        { expand: true, cwd: 'src/libs', src: ['globalize.js'], dest: '<%= pkg.gruntConfig.libTargetDir %>/' }
      ]
    }
  };
