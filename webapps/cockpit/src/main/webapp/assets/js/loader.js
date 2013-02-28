/*!
* $script Async loader & dependency manager
* https://github.com/ded/script
* (c) Dustin Diaz, Jacob Thornton 2011
* License: MIT
*/
(function(a,b){typeof module!="undefined"?module.exports=b():typeof define=="function"&&define.amd?define(a,b):this[a]=b()})("$script",function(){function q(a,b,c){for(c=0,j=a.length;c<j;++c)if(!b(a[c]))return k;return 1}function r(a,b){q(a,function(a){return!b(a)})}function s(a,b,i){function o(a){return a.call?a():d[a]}function p(){if(!--n){d[m]=1,k&&k();for(var a in f)q(a.split("|"),o)&&!r(f[a],o)&&(f[a]=[])}}a=a[l]?a:[a];var j=b&&b.call,k=j?b:i,m=j?a.join(""):b,n=a.length;return setTimeout(function(){r(a,function(a){if(h[a])return m&&(e[m]=1),h[a]==2&&p();h[a]=1,m&&(e[m]=1),t(!c.test(a)&&g?g+a+"":a,p)})},0),s}function t(c,d){var e=a.createElement("script"),f=k;e.onload=e.onerror=e[p]=function(){if(e[n]&&!/^c|loade/.test(e[n])||f)return;e.onload=e[p]=null,f=1,h[c]=2,d()},e.async=1,e.src=c,b.insertBefore(e,b.firstChild)}var a=document,b=a.getElementsByTagName("head")[0],c=/^https?:\/\//,d={},e={},f={},g,h={},i="string",k=!1,l="push",m="DOMContentLoaded",n="readyState",o="addEventListener",p="onreadystatechange";return!a[n]&&a[o]&&(a[o](m,function u(){a.removeEventListener(m,u,k),a[n]="complete"},k),a[n]="loading"),s.get=t,s.order=function(a,b,c){(function d(e){e=a.shift(),a.length?s(e,d):s(e,b,c)})()},s.path=function(a){g=a},s.ready=function(a,b,c){a=a[l]?a:[a];var e=[];return!r(a,function(a){d[a]||e[l](a)})&&q(a,function(a){return d[a]})?b():!function(a){f[a]=f[a]||[],f[a][l](b),c&&c(e)}(a.join("|")),s},s})

$script.path('assets/js/');

$script([
	'lib/jquery-1.7.2.min.js',
  ],  function() {

		$script([
			'../bootstrap/js/bootstrap.min.js',
      '../bootstrap-slider/bootstrap-slider.js',
			'lib/angular/angular.js'
			], function() {
			
				$script([
				  'lib/angular/angular-resource.js',
				  'lib/angular/angular-mocks.js',
				  '../../app/app.js',
  		    '../../app/dashboard/dashboard.js',
  		    '../../app/process-definition/processDefinition.js',
					'../../common/extensions/ngmIf.js',
					'../../common/directives/directives.js',
					'../../common/directives/errorPanel.js',
					'../../common/directives/help.js',
					'../../common/directives/requestAware.js',
          '../../common/directives/processDiagram.js',
					'../../common/resources/resources.js',
					'../../common/resources/processDefinitionResource.js',
          '../../common/resources/processInstanceResource.js',
          '../../common/resources/processDefinitionDiagramResource.js',
					'../../common/services/services.js',
					'../../common/services/uri.js',
					'../../common/services/cockpitHttpInterceptor.js',
					'../../common/services/debouncer.js',
					'../../common/services/error.js',
					'../../common/services/httpUtils.js',
					'../../common/services/requestStatus.js'

				], function() {
					 angular.bootstrap(document, ['cockpit']);
					 
					 // Due to a bug in the scenario runner
					 // (see: https://groups.google.com/forum/#!msg/angular/yslVnZh9Yjk/MLi3VGXZLeMJ)
					 // we need to add "ng-app" as class at the html-element
					 $("html").addClass("ng-app");
				});
		});

});