/**
 * dataDepend - a toolkit for implementing complex, data heavy AngularJS applications
 *
 * See https://github.com/Nikku/angular-data-depend for details.
 *
 * @version 1.0.0
 *
 * @author Nico Rehwaldt <http://github.com/Nikku>
 * @author Roman Smirnov <https://github.com/romansmirnov>
 *
 * @license (c) 2013 Nico Rehwaldt, MIT
 */

(function(angular) {
  
  function createBinding(angular) {

    var module = angular.module('dataDepend', []);

    var isArray = angular.isArray, 
        isFunction = angular.isFunction,
        isObject = angular.isObject,
        forEach = angular.forEach,
        extend = angular.extend;

    function ensureArray(elements) {
      if (!isArray(elements)) {
        return [ elements ];
      } else {
        return elements;
      }
    }

    function toArray(arrayLike) {
      return Array.prototype.slice.apply(arrayLike);
    }

    var dataDependFactory = [ '$rootScope', '$injector', '$q', function($rootScope, $injector, $q) {

      function createDataDependFactory(annotate, nextTick) {

        function createProviders(parent) {

          var providers = {};

          function get(key) {
            var v = providers[key];
            if (!v) {
              if (parent) {
                v = parent.get(key);
              }
            }

            return v;
          }

          function put(key, value) {
            if (get(key)) {
              throw new Error('[dataDepend] provider with key ' + key + ' already registered');
            }

            providers[key] = value;
          }

          return {
            local: providers, 
            get: get,
            put: put
          };
        }

        var id = 0;

        function nextId() {
          return id++;
        }

        /**
         * Create a provider using the specified options
         * 
         * @param {object} options
         *
         * @returns {object} the newly created, unregistered provider
         */
        function createProvider(options) {
          
          options = options || {};

          var produces = options.produces,
              registry = options.registry,
              dependencies = options.dependencies || [],
              factory = options.factory, 
              eager = options.eager || false;

          var parentValues = {},
              children = [],
              changed = true,
              dirty = true,
              loading = null,
              data = { $loaded: false };

          // element produced by 
          // the factory
          var provider = {
            produces: produces,
            data: data,
            get: get, 
            set: set,
            resolve: resolve,
            children: children,
            filter: filter,
            destroy: destroy,
            parentChanged: parentChanged
          };

          allDependenciesDo(function(d) {
            getProvider(d).children.push(provider);
          });

          if (eager) {
            log('resolve async');
            nextTick(function() {
              log('resolve');
              resolve();
            });
          }

          if (!factory) {
            setLoaded(options.value);
          }

          function setLoaded(newValue) {

            var oldValue = data.value;

            data.$loaded = true;
            changed = false;

            if (oldValue !== newValue) {
              data.value = newValue;
              
              log('setLoaded', oldValue, ' -> ', newValue);

              notifyParentChanged();
            }
          }

          function getTracker(name) {
            var tracker = parentValues[name];
            if (!tracker) {
              parentValues[name] = tracker = {};
            }

            return tracker;
          }

          function setLoading() {
            data.$loaded = false;
            dirty = false;
          }

          function getProvider(key) {
            var provider = registry.get(key);
            
            if (!provider) {
              throw new Error('[dataDepend] No provider for ' + key);
            }

            return provider;
          }

          function allChildrenDo(fn) {
            forEach(children, fn);
          }

          function allDependenciesDo(fn) {
            forEach(dependencies, fn);
          }

          function notifyParentChanged() {
            allChildrenDo(function(child) {
              child.parentChanged();
            });
          }

          function resolveDependencies() {
            var promises = [];

            function logValue(d, value) {
              var tracker = getTracker(d),
                  oldValue = tracker.value;

              log('resolveDependencies', d, ':', oldValue, '->', value);

              if (oldValue !== value) {
                log('resolveDependencies', 'changed');
                
                tracker.value = value;
                changed = true;
              }
            }

            allDependenciesDo(function(d) {
              var provider = getProvider(d);

              var promise = provider.resolve().then(function(value) {
                logValue(d, value);
                return value;
              });

              promises.push(promise);
            });

            return $q.all(promises).then(function() {

              var values = [];

              // best effort to receive up-to-date values
              // return the most current one
              allDependenciesDo(function(d) {
                var v = getProvider(d).get();

                logValue(d, v);

                values.push(v);
              });

              return values;
            });
          }

          function asyncLoad(reload) {
            setLoading();

            log('asyncLoad: init load');

            var promise = resolveDependencies().then(function(values) {

              log('asyncLoad dependencies resolved', values);

              if (loading !== promise) {
                log('asyncLoad: skip (new load request)');
                return loading;
              }

              var value = get();

              if (factory) {

                // call factory only if neccessary
                // (i.e. if parent variables changed, reload is explicitly set
                // or no dependencies are given)
                if (changed || reload || values.length == 0) {
                  log('asyncLoad: call factory');
                  value = factory.apply(factory, values);
                }
              }

              return value;
            }).then(function(value) {

              if (loading !== promise) {
                log('asyncLoad: skip (new load request)');
                return loading;
              }

              log('asyncLoad: load complete');

              loading = null;
              setLoaded(value);
              return value;
            });

            return promise;
          }

          /**
           * Receive a notification from the parent that it got changed
           * and update your state accordingly.
           *
           */
          function parentChanged() {

            log('parentChanged START');

            // anticipating parent change, everything ok
            if (loading) {
              log('parentChanged SKIP (loading)');
              return;
            }

            dirty = true;

            // should this provider resolve its data 
            // eagerly if it got dirty
            if (eager) {
              log('parentChanged RESOLVE async');

              nextTick(function() {
                log('parentChanged RESOLVE');
                resolve();
              });
            }

            notifyParentChanged();
          }

          function get() {
            return data.value;
          }

          /**
           * Resolve the value of this data holder
           */
          function resolve(options) {
            options = options || {};

            var reload = options.reload;

            if (dirty || reload) {
              loading = asyncLoad(reload);
            }

            if (loading) {
              log('resolve: load async');
              return loading;
            } else {
              log('resolve: load sync');
              return $q.when(get());
            }
          }

          function set(value) {
            if (factory) {
              throw new Error("[dataDepend] Cannot set value, was using factory");
            }

            setLoaded(value);
          }

          function log() {
            // var args = toArray(arguments);
            // args.unshift('[' + produces + ']');
            // args.unshift('[dataDepend]');

            // console.log.apply(console, args);
          }

          /**
           * Return a filtered view on the provider, that exposes only
           * the dependency with the given name, if multiple dependencies
           * are produced. 
           *
           * @param {object} provider the provider to filter
           * @param {string} name the produced name to filter for
           *
           * @returns {object} a view on the provider that filters for the
           *                   specified name
           *
           * @throws error if the provider is not a multi provider
           */
          function filter(name) {

            if (!isArray(provider.produces)) {
              throw new Error('[dataDepend] Provider does not produce multiple values');
            }

            var idx = provider.produces.indexOf(name),
                __get = provider.get,
                __resolve = provider.resolve;

            function filter(values) {
              if (!values) {
                return values;
              } else {
                return values[idx];
              }
            }
            
            function resolve() {
              var args = toArray(arguments);
              return __resolve.apply(null, args).then(filter);
            }

            function get() {
              var args = toArray(arguments);
              return filter(__get.apply(null, args));
            }

            var filteredProvider = angular.extend({}, provider, { 
              resolve: resolve,
              get: get
            });

            return filteredProvider;
          }

          function destroy() {
            allDependenciesDo(function(d) {
              var parent = getProvider(d),
                  parentChildren =  parent.children,
                  idx = parentChildren.indexOf(provider);

              if (idx !== -1) {
                parentChildren.splice(idx, 1);
              }
            });
          }

          return provider;
        };

        function createDataDepend(scope, inheritedProvides) {

          var providers = createProviders(inheritedProvides);

          /**
           * Registers an observer on the data object that gets called whenever 
           * any of the required observed variables change. 
           *
           * @param {String|Array<String>} variables single variable or list of 
           *                                         variables the callback depends on
           * @param {Function} callback to be invoked when any of the required variables
           *                            change
           * @return {Object} a handle to getters data, including a $loaded attribute
           *                  that reflects the load status of the getter.
           * 
           * The method accepts a single variable name or an array of variable names 
           * the provided callback depends on.
           *
           * The second parameter is the callback that is executed when the required 
           * variables change. When no variables argument is given the dependencies
           * may be provided using the [ 'a', 'b', function callback(a, b) { }]
           * notation.
           * 
           * Example: 
           *
           *      var data = dataDepend.create($scope);
           *      data.provide('a', 'A');
           *      
           *      var status = data.observe('a', function(a) {
           *        console.log('a is ' + a);
           *      });
           *  
           *      $timeout(function() {
           *        data.set('a', 'B');
           *      }, 2000);
           *
           *   prints out the following to the console: 
           *
           *      > a is A
           *      > a is B
           */
          function observe(variables, callback) {

            var name = 'provider$' + nextId();
            
            if (!callback) {
              // parse callback and variables from 
              // [ 'A', 'B', function(A, B) { ... }] notation 
              callback = variables;
              variables = annotate(callback);

              if (isArray(callback)) {
                callback = callback[callback.length - 1];
              }
            } else {
              // make sure we can use get('asdf', function(asdf) { })
              // in place of get([ 'asdf' ], function(asdf) { })
              variables = ensureArray(variables);
            }

            if (!isFunction(callback)) {
              throw new Error('[dataDepend] Must provide callback as second parameter or use [ "A", "B", function(a, b) { } ] notation');
            }

            var provider = internalCreateProvider({
              produces: name, 
              factory: callback, 
              dependencies: variables, 
              eager: true,
              registry: providers
            });

            // return handle to the
            // providers data
            return provider.data;
          }

          function internalCreateProvider(options) {
            var produces = options.produces,
                provider;

            if (!produces) {
              throw new Error("[dataDepend] Must provide produces when creating new provider");
            }

            provider = createProvider(options);

            if (isArray(produces)) {
              forEach(produces, function(name) {
                providers.put(name, provider.filter(name));
              });
            } else {
              providers.put(produces, provider);
            }

            return provider;
          }

          /**
           * Watches an expression on the scope this object was registered on
           * and publishes that value as a variable.
           *
           * The old value of the watch expression is published as {name}:old in the object.
           *
           * @param {string} name of the variable to publish
           * @param {string} expression (optional) expression used to watch on the scope (defaults to name)
           *
           * @return {object} data object representing the load status
           */
          function watchScope(name, expression) {
            expression = expression || name;

            var oldValueName = name + ':old';

            // create provider
            provide(name, scope.$eval(expression));
            provide(oldValueName, null);

            var provider = providers.get(name);
            var oldValueProvider = providers.get(oldValueName);

            scope.$watch(expression, function(newValue, oldValue) {
              if (newValue !== oldValue) {
                provider.set(newValue);

                // publish old value as {name}:old
                oldValueProvider.set(oldValue);
              }
            });

            return provider.data;
          }

          /**
           * Provide one ore more variables under well known names.
           * 
           * @param {String|Array<String>} name of the variable(s)
           * @param {Function | Object | Array } value the value to initialize the object with
           *
           * @return {Object} handle to the newly created providers data
           */
          function provide(name, value) {
            var factory, 
                variables,
                provider;

            if (providers.get(name)) {
              throw new Error('[dataDepend] provider with name ' + name + ' already registered');
            }

            if (isFunction(value) || isArray(value)) {
              // parse factory and variables from 
              // [ 'A', 'B', function(A, B) { ... }] notation 
              factory = value;
              variables = annotate(factory);
              value = undefined;

              if (isArray(factory)) {
                factory = factory[factory.length - 1];
              }
            }

            provider = internalCreateProvider({
              produces: name, 
              factory: factory,
              value: value,
              dependencies: variables, 
              registry: providers
            });

            // return handle to the
            // providers data
            return provider.data;
          }

          /**
           * Set a provided variable to the given value
           * 
           * @param {string} name of the variable
           * @param {function | object | array } value the value to initialize the object with
           */
          function set(name, value) {

            if (typeof name !== 'string') {
              throw new Error("[dataDepend] expected name to be a string, got " + name);
            }

            var provider = providers.get(name);

            if (!provider) {
              throw new Error("[dataDepend] no provider with name " + name);
            }

            provider.set(value);
          }

          function changed(name) {
            var provider = providers.get(name);

            if (!provider) {
              throw new Error('[dataDepend] Provider "' + name + '" does not exists');
            }

            provider.resolve({ reload: true });
          }

          function destroy() {
            var localProviders = providers.local;
            
            forEach(localProviders, function(provider) {
              provider.destroy();
            });
          }

          function createChild(scope) {
            return createDataDepend(scope, providers);
          }

          scope.$on('$destroy', destroy);

          return {
            $providers: providers, 

            observe: observe,
            provide: provide,
            set: set,
            changed: changed,
            watchScope: watchScope,
            newChild: createChild
          };
        }

        return {
          create: createDataDepend
        };
      }

      return createDataDependFactory($injector.annotate, function(fn) {
        $rootScope.$evalAsync(fn);
      });
    }];

    module.factory('dataDepend', dataDependFactory);

    return module;
  }

  if (typeof define === "function" && define.amd) {
    define([ "angular" ], function(angular) {
      return createBinding(angular);
    });
  } else
  if (typeof angular !== undefined) {
    createBinding(angular);
  } else {
    throw new Error("Cannot bind dataDepend: AngularJS not available on window or via AMD");
  }
})(angular);