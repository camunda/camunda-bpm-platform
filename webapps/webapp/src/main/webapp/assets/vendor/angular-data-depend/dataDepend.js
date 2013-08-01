/**
 * dataDepend - a toolkit for implementing complex, data heavy applications
 *
 * See https://github.com/Nikku/angular-data-depend for details.
 *
 * @version 1.1.0
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

    var dataProviderFactory = [ '$rootScope', '$q', function($rootScope, $q) {
      
      function createFactory(nextTick) {

        function create(options) {
          
          options = options || {};

          var name = options.name,
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
            name: name,
            data: data,
            get: get, 
            set: set,
            resolve: resolve,
            children: children, 
            parentChanged: parentChanged
          };

          allDependenciesDo(function(d) {
            getProvider(d).children.push(provider);
          });

          if (eager) {
            nextTick(function() {
              resolve();
            });
          }

          if (!factory) {
            setLoaded(options.value);
          }

          function setLoaded(v) {
            data.value = v;
            data.$loaded = true;
            changed = false;

            allChildrenDo(function(child) {
              child.parentChanged();
            });
          }

          function setLoading() {
            data.$loaded = false;
            dirty = false;
          }

          function getProvider(key) {
            var provider = registry[key];
            
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

          function resolveDependencies() {
            var promises = [];

            allDependenciesDo(function(d) {
              var provider = getProvider(d);

              var promise = provider.resolve().then(function(value) {

                var oldValue = parentValues[d];
                if (oldValue != value) {
                  parentValues[d] = value;
                  changed = true;
                }

                return value;
              });

              promises.push(promise);
            });

            return promises;
          }

          function asyncLoad(reload) {
            setLoading();

            var promise = $q.all(resolveDependencies()).then(function(values) {

              var value = get();

              if (factory) {

                // call factory only if neccessary
                // (i.e. if parent variables changed, reload is explicitly set
                // or no dependencies are given)
                if (changed || reload || values.length == 0) {
                  value = factory.apply(factory, values);
                }
              }

              return value;
            }).then(function(value) {
              if (loading === promise) {
                loading = null;
              }

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
            
            // anticipating parent change, everything ok
            if (loading) {
              return;
            }

            dirty = true;

            // should this provider resolve its data 
            // eagerly if it got dirty
            if (eager) {
              nextTick(function() {
                resolve();
              });
            }

            allChildrenDo(function(child) {
              child.parentChanged();
            });
          }

          function get() {
            return data.value;
          }

          /**
           * Resolve the value of this data holder
           */
          function resolve(options) {
            var reload = (options || {}).reload;

            if (dirty || reload) {
              loading = asyncLoad(reload);
            }

            if (loading) {
              return loading;
            } else {
              return $q.when(get());
            }
          }

          function set(value) {
            if (factory) {
              throw new Error("[dataDepend] Cannot set value, was using factory");
            }

            setLoaded(value);
          }

          return provider;
        };

        // factory
        return {
          create: create
        };
      }

      return createFactory(function(fn) {
        $rootScope.$evalAsync(fn);
      });
    }];

    var dataDependFactory = [ '$rootScope', '$injector', 'dataProviderFactory', function($rootScope, $injector, dataProviderFactory) {

      function createFactory(annotate, nextTick) {

        function create() {

          var nextId = 0;
          var providers = {};

          function get(variables, callback) {

            var name = 'provider$' + nextId++;
            
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
              name: name, 
              factory: callback, 
              dependencies: variables, 
              eager: true,
              registry: providers
            })

            // return handle to the
            // providers data
            return provider.data;
          }

          function internalCreateProvider(options) {
            var name = options.name,
                provider;

            if (!name) {
              throw new Error("[dataDepend] Must provide name when creating new provider");
            }

            provider = dataProviderFactory.create(options);
            providers[name] = provider;

            return provider;
          }

          function set(name, value) {
            var provider = providers[name],
                factory, 
                variables;

            if (provider) {
              provider.set(value);
              return;
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

            var provider = internalCreateProvider({
              name: name, 
              factory: factory,
              value: value,
              dependencies: variables, 
              registry: providers
            });

            // return handle to the
            // providers data
            return provider.data;
          }

          function changed(name) {
            var provider = providers[name];

            if (!provider) {
              throw new Error('[dataDepend] Provider "' + name + '" does not exists');
            }

            provider.resolve({ reload: true });
          }

          return {
            $providers: providers, 

            get: get,
            set: set,
            changed: changed
          };
        }

        return {
          create: create
        };
      }

      return createFactory($injector.annotate, function(fn) {
        $rootScope.$evalAsync(fn);
      });
    }];

    module.factory('dataDependFactory', dataDependFactory);
    module.factory('dataProviderFactory', dataProviderFactory);

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