/**
 * @file better-dom
 * @version 1.1.0 2013-07-07T22:56:42
 * @overview Sandbox for DOM extensions
 * @copyright Maksim Chemerisuk 2013
 * @license MIT
 * @see https://github.com/chemerisuk/better-dom
 */
(function(window, document, documentElement, undefined) {
    "use strict";
    
    // HELPERS
    // -------

    // jshint unused:false
    var _uniqueId = (function() {
            var idCounter = 1;

            return function(prefix) {
                return (prefix || "") + idCounter++;
            };
        })(),
        _defer = function(callback) {
            return setTimeout(callback, 0);
        },
        _makeError = function(method, el) {
            var type;

            if (el instanceof $Element) {
                type = "$Element";
            } else {
                type = "DOM";
            }

            return "Error: " + type + "." + method + " was called with illegal arguments. Check http://chemerisuk.github.io/better-dom/" + type + ".html#" + method + " to verify the function call";
        },
        makeLoopMethod = (function(){
            var rcallback = /cb\.call\(([^)]+)\)/g,
                defaults = {
                    BEFORE: "",
                    COUNT:  "a ? a.length : 0",
                    BODY:   "",
                    AFTER:  ""
                };

            return function(options) {
                var code = "%BEFORE%\nfor(var i=0,n=%COUNT%;i<n;++i){%BODY%}%AFTER%";

                _forIn(defaults, function(value, key) {
                    code = code.replace("%" + key + "%", options[key] || value);
                });

                // improve callback invokation by using call on demand
                code = code.replace(rcallback, function(expr, args) {
                    return "(that?" + expr + ":cb(" + args.split(",").slice(1).join() + "))";
                });

                return Function("a", "cb", "that", "undefined", code);
            };
        })(),

        // OBJECT UTILS
        // ------------
        
        _forIn = function(obj, callback, thisPtr) {
            for (var prop in obj) {
                callback.call(thisPtr, obj[prop], prop, obj);
            }
        },
        _forOwn = (function() {
            if (Object.keys) {
                return makeLoopMethod({
                    BEFORE: "var keys = Object.keys(a), k",
                    COUNT:  "keys.length",
                    BODY:   "k = keys[i]; cb.call(that, a[k], k, a)"
                });
            } else {
                return function(obj, callback, thisPtr) {
                    for (var prop in obj) {
                        if (Object.prototype.hasOwnProperty.call(obj, prop)) callback.call(thisPtr, obj[prop], prop, obj);
                    }
                };
            }
        }()),
        _keys = Object.keys || (function() {
            var collectKeys = function(value, key) { this.push(key); };

            return function(obj) {
                var result = [];

                _forOwn(obj, collectKeys, result);

                return result;
            };
        }()),
        _extend = function(obj, mixins) {
            _forOwn(mixins, function(value, key) {
                obj[key] = value;
            });

            return obj;
        },

        // COLLECTION UTILS
        // ----------------
        
        _forEach = makeLoopMethod({
            BODY:   "cb.call(that, a[i], i, a)",
            AFTER:  "return a"
        }),
        _map = makeLoopMethod({
            BEFORE: "var out = []",
            BODY:   "out.push(cb.call(that, a[i], i, a))",
            AFTER:  "return out"
        }),
        _some = makeLoopMethod({
            BODY:   "if (cb.call(that, a[i], i, a) === true) return true",
            AFTER:  "return false"
        }),
        _filter = makeLoopMethod({
            BEFORE: "var out = []",
            BODY:   "if (cb.call(that, a[i], i, a)) out.push(a[i])",
            AFTER:  "return out"
        }),
        _foldl = makeLoopMethod({
            BODY:   "that = (!i && that === undefined ? a[i] : cb(that, a[i], i, a))",
            AFTER:  "return that"
        }),
        _foldr = makeLoopMethod({
            BEFORE: "var j",
            BODY:   "j = n - i - 1; that = (!i && that === undefined ? a[j] : cb(that, a[j], j, a))",
            AFTER:  "return that"
        }),
        _every = makeLoopMethod({
            BEFORE: "var out = true",
            BODY:   "out = cb.call(that, a[i], i, a) && out",
            AFTER:  "return out"
        }),
        _slice = function(list, index) {
            return Array.prototype.slice.call(list, index || 0);
        },

        // DOM UTILS
        // ---------

        _getComputedStyle = function(el) {
            return window.getComputedStyle ? window.getComputedStyle(el) : el.currentStyle;
        },
        _createElement = function(tagName) {
            return document.createElement(tagName);
        },
        _createFragment = function() {
            return document.createDocumentFragment();
        },
        _parseFragment = (function() {
            var parser = document.createElement("body");

            if (!document.addEventListener) {
                // Add html5 elements support via:
                // https://github.com/aFarkas/html5shiv
                (function(){
                    var elements = "abbr article aside audio bdi canvas data datalist details figcaption figure footer header hgroup main mark meter nav output progress section summary template time video",
                        // Used to skip problem elements
                        reSkip = /^<|^(?:button|map|select|textarea|object|iframe|option|optgroup)$/i,
                        // Not all elements can be cloned in IE
                        saveClones = /^(?:a|b|code|div|fieldset|h1|h2|h3|h4|h5|h6|i|label|li|ol|p|q|span|strong|style|table|tbody|td|th|tr|ul)$/i,
                        create = document.createElement,
                        frag = _createFragment(),
                        cache = {};

                    frag.appendChild(parser);

                    _createElement = function(nodeName) {
                        var node;

                        if (cache[nodeName]) {
                            node = cache[nodeName].cloneNode();
                        } else if (saveClones.test(nodeName)) {
                            node = (cache[nodeName] = create(nodeName)).cloneNode();
                        } else {
                            node = create(nodeName);
                        }

                        return node.canHaveChildren && !reSkip.test(nodeName) ? frag.appendChild(node) : node;
                    };

                    _createFragment = Function("f", "return function(){" +
                        "var n=f.cloneNode(),c=n.createElement;" +
                        "(" +
                            // unroll the `createElement` calls
                            elements.split(" ").join().replace(/\w+/g, function(nodeName) {
                                create(nodeName);
                                frag.createElement(nodeName);
                                return "c('" + nodeName + "')";
                            }) +
                        ");return n}"
                    )(frag);
                })();
            }

            return function(html) {
                var fragment = _createFragment();

                // fix NoScope bug
                parser.innerHTML = "<br/>" + html;
                parser.removeChild(parser.firstChild);

                while (parser.firstChild) {
                    fragment.appendChild(parser.firstChild);
                }

                return fragment;
            };
        })();

    // DOM NODE
    // --------

    /**
     * Prototype for a DOM node
     * @name $Node
     * @param node native object
     * @constructor
     * @private
     */
    function $Node(node) {
        if (node) {
            this._node = node;
            this._data = {};
            this._listeners = [];

            node.__dom__ = this;
        }
    }

    $Node.prototype = {
        constructor: $Node
    };

    /**
     * Check element capability
     * @param {String} prop property to check
     * @param {String} [tag] name of element to test
     * @return {Boolean} true, if feature is supported
     * @example
     * input.supports("placeholder");
     * // => true if an input supports placeholders
     * DOM.supports("addEventListener");
     * // => true if browser supports document.addEventListener
     * DOM.supports("oninvalid", "input");
     * // => true if browser supports `invalid` event
     */
    $Node.prototype.supports = function(prop, tagName) {
        // http://perfectionkills.com/detecting-event-support-without-browser-sniffing/
        var node = _createElement(tagName || this._node.tagName || "div"),
            isSupported = prop in node;

        if (!isSupported && !prop.indexOf("on")) {
            node.setAttribute(prop, "return;");

            isSupported = typeof node[prop] === "function";
        }
            
        return isSupported;
    };

    // SEARCH BY QUERY
    // ---------------

    (function() {
        // big part of code inspired by Sizzle:
        // https://github.com/jquery/sizzle/blob/master/sizzle.js

        // TODO: disallow to use buggy selectors?
        var rquickExpr = /^(?:#([\w\-]+)|(\w+)|\.([\w\-]+))$/,
            rsibling = /[\x20\t\r\n\f]*[+~>]/,
            rescape = /'|\\/g,
            tmpId = _uniqueId("DOM");

        if (!document.getElementsByClassName) {
            // exclude getElementsByClassName from pattern
            rquickExpr = /^(?:#([\w\-]+)|(\w+))$/;
        }
        
        /**
         * Finds element by selector
         * @param  {String} selector css selector
         * @return {$Element} element or null if nothing was found
         * @example
         * var domBody = DOM.find("body");
         *
         * domBody.find("#element");
         * // returns $Element with id="element"
         * domBody.find(".link");
         * // returns first element with class="link"
         */
        $Node.prototype.find = function(selector, /*INTERNAL*/multiple) {
            if (typeof selector !== "string") {
                throw _makeError("find", this);
            }

            var node = this._node,
                quickMatch, m, elem, elements, old, nid, context;

            if (quickMatch = rquickExpr.exec(selector)) {
                // Speed-up: "#ID"
                if (m = quickMatch[1]) {
                    elem = document.getElementById(m);
                    // Handle the case where IE, Opera, and Webkit return items
                    // by name instead of ID
                    if ( elem && elem.parentNode && elem.id === m && (node === document || this.contains(elem)) ) {
                        elements = [elem];
                    }
                // Speed-up: "TAG"
                } else if (quickMatch[2]) {
                    elements = node.getElementsByTagName(selector);
                // Speed-up: ".CLASS"
                } else if (m = quickMatch[3]) {
                    elements = node.getElementsByClassName(m);
                }

                if (elements && !multiple) {
                    elements = elements[0];
                }
            } else {
                old = true,
                nid = tmpId,
                context = node;

                if (node !== document) {
                    // qSA works strangely on Element-rooted queries
                    // We can work around this by specifying an extra ID on the root
                    // and working up from there (Thanks to Andrew Dupont for the technique)
                    if ( (old = node.getAttribute("id")) ) {
                        nid = old.replace(rescape, "\\$&");
                    } else {
                        node.setAttribute("id", nid);
                    }

                    nid = "[id='" + nid + "'] ";

                    context = rsibling.test(selector) && node.parentNode || node;
                    selector = nid + selector.split(",").join("," + nid);
                }

                try {
                    elements = context[multiple ? "querySelectorAll" : "querySelector"](selector);
                } finally {
                    if ( !old ) {
                        node.removeAttribute("id");
                    }
                }
            }

            return multiple ? new $CompositeElement(elements) : $Element(elements);
        };

        /**
         * Finds all elements by selector
         * @param  {String} selector css selector
         * @return {$CompositeElement} elements collection
         */
        $Node.prototype.findAll = function(selector) {
            return this.find(selector, true);
        };
    })();

    // INTERNAL DATA
    // -------------

    (function() {
        var processObjectParam = function(value, name) { this.setData(name, value); };

        /**
         * Read data entry value
         * @param  {String} key data entry key
         * @return {Object} data entry value
         * @example
         * var domLink = DOM.find(".link");
         *
         * domLink.setData("test", "message");
         * domLink.getData("test");
         * // returns string "message"
         */
        $Node.prototype.getData = function(key) {
            if (typeof key !== "string") {
                throw _makeError("getData", this);
            }

            var node = this._node,
                result = this._data[key];

            if (result === undefined && node.hasAttribute("data-" + key)) {
                result = this._data[key] = node.getAttribute("data-" + key);
            }

            return result;
        };

        /**
         * Store data entry value(s)
         * @param {String|Object} key data entry key | key/value pairs
         * @param {Object} value data to store
         * @return {$Node}
         * @example
         * var domLink = DOM.find(".link");
         *
         * domLink.setData("test", "message");
         * domLink.setData({a: "b", c: "d"});
         */
        $Node.prototype.setData = function(key, value) {
            var keyType = typeof key;

            if (keyType === "string") {
                this._data[key] = value;
            } else if (keyType === "object") {
                _forOwn(key, processObjectParam, this);
            } else {
                throw _makeError("setData", this);
            }

            return this;
        };
    })();

    // CONTAINS
    // --------

    (function() {
        var containsElement;

        if (documentElement.contains) {
            containsElement = function(parent, child) {
                return parent.contains(child);
            };
        } else {
            containsElement = function(parent, child) {
                return !!(parent.compareDocumentPosition(child) & 16);
            };
        }
        
        /**
         * Check if element is inside of context
         * @param  {$Element} element element to check
         * @return {Boolean} true if success
         * @example
         * DOM.find("html").contains(DOM.find("body"));
         * // returns true
         */
        $Node.prototype.contains = function(element) {
            var node = this._node, result;

            if (element.nodeType === 1) {
                result = containsElement(node, element);
            } else if (element instanceof $Element) {
                result = element.every(function(element) {
                    return containsElement(node, element._node);
                });
            } else {
                throw _makeError("contains", this);
            }

            return result;
        };
    })();

    // DOM EVENTS
    // ----------

    (function() {
        var eventHooks = {},
            rpropexpr = /^([a-z:]+)(?:\(([^)]+)\))?\s?(.*)$/,
            legacyCustomEventName = "dataavailable",
            processObjectParam = function(value, name) { this.on(name, value); },
            createCustomEventWrapper = function(originalHandler, type) {
                var handler = function() {
                        if (window.event._type === type) originalHandler();
                    };

                handler.type = originalHandler.type;
                handler._type = legacyCustomEventName;
                handler.callback = originalHandler.callback;

                return handler;
            };

        /**
         * Bind a DOM event to the context
         * @param  {String}   type event type
         * @param  {Object}   [context] callback context
         * @param  {Function|String} callback event callback
         * @param  {Array}    [args] extra arguments
         * @return {$Node}
         * @example
         * // NOTICE: handler don't have e as the first argument
         * input.on("click", function() {...});
         * // NOTICE: event arguments in event name
         * input.on("keydown(keyCode,altKey)", function(keyCode, altKey) {...});
         */
        $Node.prototype.on = function(type, context, callback, args) {
            var eventType = typeof type,
                hook, handler, selector, expr;

            if (eventType === "string") {
                if (typeof context !== "object") {
                    args = callback;
                    callback = context;
                    context = this;
                }

                expr = rpropexpr.exec(type);
                type = expr[1];
                selector = expr[3];
                
                handler = EventHandler(expr, context, callback, args, this._node);
                handler.type = selector ? type + " " + selector : type;
                handler.callback = callback;
                handler.context = context;

                if (hook = eventHooks[type]) hook(handler);

                if (document.addEventListener) {
                    this._node.addEventListener(handler._type || type, handler, !!handler.capturing);
                } else {
                    // handle custom events for IE8
                    if (!this.supports("on" + type) || handler.custom) handler = createCustomEventWrapper(handler, type);

                    this._node.attachEvent("on" + (handler._type || type), handler);
                }
                // store event entry
                this._listeners.push(handler);
            } else if (eventType === "object") {
                _forOwn(type, processObjectParam, this);
            } else {
                throw _makeError("on", this);
            }

            return this;
        };

        /**
         * Unbind a DOM event from the context
         * @param  {String}          type event type
         * @param  {Object}          [context] callback context
         * @param  {Function|String} [callback] event handler
         * @return {$Node}
         */
        $Node.prototype.off = function(type, context, callback) {
            if (typeof type !== "string") {
                throw _makeError("off", this);
            }

            if (typeof context !== "object") {
                callback = context;
                context = !callback ? undefined : this;
            }

            _forEach(this._listeners, function(handler, index, events) {
                var node = this._node;

                if (handler && type === handler.type && (!context || context === handler.context) && (!callback || callback === handler.callback)) {
                    type = handler._type || handler.type;

                    if (document.removeEventListener) {
                        node.removeEventListener(type, handler, !!handler.capturing);
                    } else {
                        node.detachEvent("on" + type, handler);
                    }
                    
                    delete events[index];
                }
            }, this);

            return this;
        };

        /**
         * Triggers an event of specific type
         * @param  {String} eventType type of event
         * @param  {Object} [detail] event details
         * @return {$Node}
         * @example
         * var domLink = DOM.find(".link");
         *
         * domLink.fire("focus");
         * // receive focus to the element
         * domLink.fire("custom:event", {x: 1, y: 2});
         * // trigger a custom:event on the element
         */
        $Node.prototype.fire = function(type, detail) {
            if (typeof type !== "string") {
                throw _makeError("fire", this);
            }

            var node = this._node,
                hook = eventHooks[type],
                handler = {},
                isCustomEvent, canContinue, event;

            if (hook) hook(handler);

            isCustomEvent = handler.custom || !this.supports("on" + type);

            if (document.createEvent) {
                event = document.createEvent("HTMLEvents");

                event.initEvent(handler._type || type, true, true);
                event.detail = detail;

                canContinue = node.dispatchEvent(event);
            } else {
                event = document.createEventObject();

                // store original event type
                event._type = isCustomEvent ? type : undefined;
                event.detail = detail;

                node.fireEvent("on" + (isCustomEvent ? legacyCustomEventName : handler._type || type), event);

                canContinue = event.returnValue !== false;
            }

            // Call a native DOM method on the target with the same name as the event
            // IE<9 dies on focus/blur to hidden element
            if (canContinue && node[type] && (type !== "focus" && type !== "blur" || node.offsetWidth)) {
                // Prevent re-triggering of the same event
                EventHandler.veto = type;
                
                node[type]();

                EventHandler.veto = false;
            }

            return this;
        };

        // firefox doesn't support focusin/focusout events
        if ($Node.prototype.supports("onfocusin", "input")) {
            _forOwn({focus: "focusin", blur: "focusout"}, function(value, prop) {
                eventHooks[prop] = function(handler) { handler._type = value; };
            });
        } else {
            eventHooks.focus = eventHooks.blur = function(handler) {
                handler.capturing = true;
            };
        }

        if ($Node.prototype.supports("validity", "input")) {
            eventHooks.invalid = function(handler) {
                handler.capturing = true;
            };
        }

        if (!document.addEventListener) {
            // input event fix via propertychange
            document.attachEvent("onfocusin", (function() {
                var propertyChangeEventHandler = function() {
                        var e = window.event;

                        if (e.propertyName === "value") {
                            // trigger special event that bubbles
                            $Element(e.srcElement).fire("input");
                        }
                    },
                    capturedNode;

                return function() {
                    var target = window.event.srcElement,
                        type = target.type;

                    if (capturedNode) {
                        capturedNode.detachEvent("onpropertychange", propertyChangeEventHandler);
                        capturedNode = undefined;
                    }

                    if (type === "text" || type === "password" || type === "textarea") {
                        (capturedNode = target).attachEvent("onpropertychange", propertyChangeEventHandler);
                    }
                };
            })());

            // submit event bubbling fix
            document.attachEvent("onkeydown", function() {
                var e = window.event,
                    target = e.srcElement,
                    form = target.form;

                if (form && target.type !== "textarea" && e.keyCode === 13 && e.returnValue !== false) {
                    $Element(form).fire("submit");

                    return false;
                }
            });

            document.attachEvent("onclick", (function() {
                var handleSubmit = function() {
                        var form = window.event.srcElement;

                        form.detachEvent("onsubmit", handleSubmit);

                        $Element(form).fire("submit");

                        return false;
                    };

                return function() {
                    var target = window.event.srcElement,
                        form = target.form;

                    if (form && target.type === "submit") {
                        form.attachEvent("onsubmit", handleSubmit);
                    }
                };
            })());

            eventHooks.submit = function(handler) {
                handler.custom = true;
            };
        }
    }());

    /**
     * Helper for css selectors
     * @private
     * @constructor
     */
    var SelectorMatcher = (function() {
        // Quick matching inspired by
        // https://github.com/jquery/jquery
        var rquickIs = /^(\w*)(?:#([\w\-]+))?(?:\[([\w\-]+)\])?(?:\.([\w\-]+))?$/,
            ctor =  function(selector) {
                if (this instanceof SelectorMatcher) {
                    this.selector = selector;

                    var quick = rquickIs.exec(selector);
                    // TODO: support attribute value check
                    if (this.quick = quick) {
                        //   0  1    2   3          4
                        // [ _, tag, id, attribute, class ]
                        if (quick[1]) quick[1] = quick[1].toLowerCase();
                        if (quick[4]) quick[4] = " " + quick[4] + " ";
                    }
                } else {
                    return selector ? new SelectorMatcher(selector) : null;
                }
            },
            matchesProp = _foldl("m oM msM mozM webkitM".split(" "), function(result, prefix) {
                var propertyName = prefix + "atchesSelector";

                return result || documentElement[propertyName] && propertyName;
            }, null),
            matches = (function() {
                var isEqual = function(val) { return val === this; };

                return function(el, selector) {
                    return _some(document.querySelectorAll(selector), isEqual, el);
                };
            }());

        ctor.prototype = {
            test: function(el) {
                if (this.quick) {
                    return (
                        (!this.quick[1] || (el.nodeName || "").toLowerCase() === this.quick[1]) &&
                        (!this.quick[2] || el.id === this.quick[2]) &&
                        (!this.quick[3] || el.hasAttribute(this.quick[3])) &&
                        (!this.quick[4] || !!~((" " + (el.className || "") + " ").indexOf(this.quick[4])))
                    );
                }

                return matchesProp ? el[matchesProp](this.selector) : matches(el, this.selector);
            }
        };

        return ctor;
    })();

    /**
     * Helper type to create an event handler
     * @private
     * @constructor
     */
    var EventHandler = (function() {
        var hooks = {}, legacyIE = !document.addEventListener;

        hooks.currentTarget = function(event, currentTarget) {
            return $Element(currentTarget);
        };

        if (legacyIE) {
            hooks.target = function(event) {
                return $Element(event.srcElement);
            };
        } else {
            hooks.target = function(event) {
                return $Element(event.target);
            };
        }
        
        if (legacyIE) {
            hooks.relatedTarget = function(event, currentTarget) {
                var propName = ( event.toElement === currentTarget ? "from" : "to" ) + "Element";

                return $Element(event[propName]);
            };
        } else {
            hooks.relatedTarget = function(event) {
                return $Element(event.relatedTarget);
            };
        }

        if (legacyIE) {
            hooks.defaultPrevented = function(event) {
                return event.returnValue === false;
            };
        }

        return function(expr, context, callback, extras, currentTarget) {
            var matcher = SelectorMatcher(expr[3]),
                isCallbackProp = typeof callback === "string",
                defaultEventHandler = function(e) {
                    if (EventHandler.veto !== expr[1]) {
                        var event = e || window.event,
                            fn = isCallbackProp ? context[callback] : callback,
                            result, args;

                        // populate event handler arguments
                        if (expr[2]) {
                            args = _map(expr[2].split(","), function(name) {
                                if (name === "type") return expr[1];

                                var hook = hooks[name];

                                return hook ? hook(event, currentTarget) : event[name];
                            });
                            
                            if (extras) args.push.apply(args, extras);
                        } else {
                            args = extras ? extras.slice(0) : [];
                        }

                        // make performant call
                        if (args.length) {
                            if (fn) result = fn.apply(context, args);
                        } else {
                            result = isCallbackProp ? fn && context[callback]() : fn.call(context);
                        }

                        // prevent default if handler returns false
                        if (result === false) {
                            if (event.preventDefault) {
                                event.preventDefault();
                            } else {
                                event.returnValue = false;
                            }
                        }
                    }
                };

            return !matcher ? defaultEventHandler : function(e) {
                var node = window.event ? window.event.srcElement : e.target;

                for (; node && node !== currentTarget; node = node.parentNode) {
                    if (matcher.test(node)) return defaultEventHandler(e);
                }
            };
        };
    }());

    
    // DOM ELEMENT
    // -----------

    /**
     * Prototype for a DOM element
     * @name $Element
     * @param element native element
     * @extends $Node
     * @constructor
     * @private
     */
    function $Element(element) {
        if (element && element.__dom__) return element.__dom__;

        if (!(this instanceof $Element)) {
            return element ? new $Element(element) : new $CompositeElement();
        }

        $Node.call(this, element);

        if (element) Array.prototype.push.call(this, this);
    }

    $Element.prototype = new $Node();
    $Element.prototype.constructor = $Element;

    // CLASSES MANIPULATION
    // --------------------

    (function() {
        var rclass = /[\n\t\r]/g;

        function makeClassesMethod(nativeStrategyName, strategy) {
            var methodName = nativeStrategyName === "contains" ? "hasClass" : nativeStrategyName + "Class";

            if (documentElement.classList) {
                strategy = function(className) {
                    return this._node.classList[nativeStrategyName](className);
                };
            }

            strategy = (function(strategy){
                return function(className) {
                    if (typeof className !== "string") throw _makeError(methodName, this);

                    return strategy.call(this, className);
                };
            })(strategy);

            if (methodName === "hasClass") {
                return function() {
                    return _every(arguments, strategy, this);
                };
            } else {
                return function() {
                    _forEach(arguments, strategy, this);

                    return this;
                };
            }
        }

        /**
         * Check if element contains class name(s)
         * @param  {...String} classNames class name(s)
         * @return {Boolean}   true if the element contains all classes
         * @function
         */
        $Element.prototype.hasClass = makeClassesMethod("contains", function(className) {
            return !!~((" " + this._node.className + " ")
                        .replace(rclass, " ")).indexOf(" " + className + " ");
        });

        /**
         * Add class(es) to element
         * @param  {...String} classNames class name(s)
         * @return {$Element}
         * @function
         */
        $Element.prototype.addClass = makeClassesMethod("add", function(className) {
            if (!this.hasClass(className)) {
                this._node.className += " " + className;
            }
        });

        /**
         * Remove class(es) from element
         * @param  {...String} classNames class name(s)
         * @return {$Element}
         * @function
         */
        $Element.prototype.removeClass = makeClassesMethod("remove", function(className) {
            className = (" " + this._node.className + " ")
                    .replace(rclass, " ").replace(" " + className + " ", " ");

            this._node.className = className.substr(className[0] === " " ? 1 : 0, className.length - 2);
        });

        /**
         * Toggle class(es) on element
         * @param  {...String}  classNames class name(s)
         * @return {$Element}
         * @function
         */
        $Element.prototype.toggleClass = makeClassesMethod("toggle", function(className) {
            var oldClassName = this._node.className;

            this.addClass(className);

            if (oldClassName === this._node.className) {
                this.removeClass(className);
            }
        });
    })();

    /**
     * Clone element
     * @return {$Element} clone of current element
     */
    $Element.prototype.clone = function() {
        var node;

        if (document.addEventListener) {
            node = this._node.cloneNode(true);
        } else {
            node = _createElement("div");
            node.innerHTML = this._node.outerHTML;
            node = node.firstChild;
        }
        
        return new $Element(node);
    };

    // MANIPULATION
    // ------------
    
    (function() {
        function makeManipulationMethod(methodName, fasterMethodName, strategy) {
            // always use _parseFragment because of HTML5 and NoScope bugs in IE
            if (document.attachEvent) fasterMethodName = false;

            return function(value) {
                var valueType = typeof value,
                    node = this._node,
                    relatedNode = node.parentNode;

                if (valueType === "function") {
                    value = value.call(this);
                    valueType = typeof value;
                }

                if (valueType === "string") {
                    if (value[0] !== "<") value = DOM.parseTemplate(value);

                    relatedNode = fasterMethodName ? null : _parseFragment(value);
                } else if (value && (value.nodeType === 1 || value.nodeType === 11)) {
                    relatedNode = value;
                } else if (value instanceof $Element) {
                    value.each(function(el) { this[methodName](el._node); }, this);

                    return this;
                } else if (value !== undefined) {
                    throw _makeError(methodName, this);
                }

                if (relatedNode) {
                    strategy(node, relatedNode);
                } else {
                    node.insertAdjacentHTML(fasterMethodName, value);
                }

                return this;
            };
        }

        /**
         * Insert html string or native element after the current
         * @param {String|Element|$Element} content HTML string or Element
         * @return {$Element}
         * @function
         */
        $Element.prototype.after = makeManipulationMethod("after", "afterend", function(node, relatedNode) {
            node.parentNode.insertBefore(relatedNode, node.nextSibling);
        });

        /**
         * Insert html string or native element before the current
         * @param {String|Element|$Element} content HTML string or Element
         * @return {$Element}
         * @function
         */
        $Element.prototype.before = makeManipulationMethod("before", "beforebegin", function(node, relatedNode) {
            node.parentNode.insertBefore(relatedNode, node);
        });

        /**
         * Prepend html string or native element to the current
         * @param {String|Element|$Element} content HTML string or Element
         * @return {$Element}
         * @function
         */
        $Element.prototype.prepend = makeManipulationMethod("prepend", "afterbegin", function(node, relatedNode) {
            node.insertBefore(relatedNode, node.firstChild);
        });

        /**
         * Append html string or native element to the current
         * @param {String|Element|$Element} content HTML string or Element
         * @return {$Element}
         * @function
         */
        $Element.prototype.append = makeManipulationMethod("append", "beforeend", function(node, relatedNode) {
            node.appendChild(relatedNode);
        });

        /**
         * Replace current element with html string or native element
         * @param {String|Element|$Element} content HTML string or Element
         * @return {$Element}
         * @function
         */
        $Element.prototype.replace = makeManipulationMethod("replace", "", function(node, relatedNode) {
            node.parentNode.replaceChild(relatedNode, node);
        });

        /**
         * Remove current element from DOM
         * @return {$Element}
         * @function
         */
        $Element.prototype.remove = makeManipulationMethod("remove", "", function(node, parentNode) {
            parentNode.removeChild(node);
        });
    })();

    /**
     * Check if the element matches selector
     * @param  {String} selector css selector
     * @return {$Element}
     */
    $Element.prototype.matches = function(selector) {
        if (!selector || typeof selector !== "string") {
            throw _makeError("matches", this);
        }

        return new SelectorMatcher(selector).test(this._node);
    };

    
    /**
     * Calculates offset of current context
     * @return {{top: Number, left: Number, right: Number, bottom: Number}} offset object
     */
    $Element.prototype.offset = function() {
        var bodyElement = document.body,
            boundingRect = this._node.getBoundingClientRect(),
            clientTop = documentElement.clientTop || bodyElement.clientTop || 0,
            clientLeft = documentElement.clientLeft || bodyElement.clientLeft || 0,
            scrollTop = window.pageYOffset || documentElement.scrollTop || bodyElement.scrollTop,
            scrollLeft = window.pageXOffset || documentElement.scrollLeft || bodyElement.scrollLeft;

        return {
            top: boundingRect.top + scrollTop - clientTop,
            left: boundingRect.left + scrollLeft - clientLeft,
            right: boundingRect.right + scrollLeft - clientLeft,
            bottom: boundingRect.bottom + scrollTop - clientTop
        };
    };

    // GETTER
    // ------

    (function() {
        var hooks = {};

        /**
         * Get property or attribute by name
         * @param  {String} [name] property/attribute name
         * @return {String} property/attribute value
         * @example
         * // returns value of the id property (i.e. "link" string)
         * link.get("id");
         * // returns value of "data-attr" attribute
         * link.get("data-attr");
         * // returns innerHTML of the element
         * link.get();
         */
        $Element.prototype.get = function(name) {
            var node = this._node,
                hook = hooks[name];

            if (name === undefined) {
                if (node.tagName === "OPTION") {
                    name = node.hasAttribute("value") ? "value" : "text";
                } else {
                    name = node.type && "value" in node ? "value" : "innerHTML";
                }
            } else if (typeof name !== "string") {
                throw _makeError("get", this);
            }

            return hook ? hook(node, name) : (name in node ? node[name] : node.getAttribute(name));
        };

        hooks.tagName = hooks.method = function(node, key) {
            return node[key].toLowerCase();
        };

        hooks.elements = hooks.options = function(node, key) {
            return new $CompositeElement(node[key]);
        };

        hooks.form = function(node) {
            return $Element(node.form);
        };

        hooks.type = function(node) {
            // some browsers don't recognize input[type=email] etc.
            return node.getAttribute("type") || node.type;
        };
    })();

    // SETTER
    // ------

    (function() {
        var hooks = {},
            processObjectParam = function(value, name) { this.set(name, value); };

        /**
         * Set property/attribute value
         * @param {String} [name] property/attribute name
         * @param {String} value property/attribute value
         * @return {$Element}
         * @example
         * // sets property href (and that action updates attribute value too)
         * link.set("href", "/some/path");
         * // sets attribute "data-attr" to "123"
         * link.set("data-attr", "123");
         * // sets innerHTML to "some text"
         * link.set("some text");
         */
        $Element.prototype.set = function(name, value) {
            var node = this._node,
                nameType = typeof name;

            if (nameType === "string") {
                if (value === undefined) {
                    value = name;

                    if (node.type && "value" in node) {
                        // for IE use innerText because it doesn't trigger onpropertychange
                        name = window.addEventListener ? "value" : "innerText";
                    } else {
                        name = "innerHTML";
                    }
                }

                if (typeof value === "function") {
                    value = value.call(this, value.length ? this.get(name) : undefined);
                }

                _forEach(name.split(" "), function(name) {
                    var hook = hooks[name];

                    if (hook) {
                        hook(node, value);
                    } else if (value === null) {
                        node.removeAttribute(name);
                    } else if (name in node) {
                        node[name] = value;
                    } else {
                        node.setAttribute(name, value);
                    }
                });
            } else if (nameType === "object") {
                _forOwn(name, processObjectParam, this);
            } else {
                throw _makeError("set", this);
            }

            return this;
        };

        if (document.attachEvent) {
            // fix NoScope elements in IE < 10
            hooks.innerHTML = function(node, value) {
                node.innerHTML = "";
                node.appendChild(_parseFragment(value));
            };
            
            // fix hidden attribute for IE < 10
            hooks.hidden = function(node, value) {
                if (typeof value !== "boolean") {
                    throw _makeError("set", this);
                }

                node.hidden = value;

                if (value) {
                    node.setAttribute("hidden", "hidden");
                } else {
                    node.removeAttribute("hidden");
                }

                // trigger redraw in IE
                node.style.zoom = value ? "1" : "0";
            };
        }
    })();

    // STYLES MANIPULATION
    // -------------------
    
    (function() {
        var getStyleHooks = {},
            setStyleHooks = {},
            reDash = /\-./g,
            reCamel = /[A-Z]/g,
            dashSeparatedToCamelCase = function(str) { return str[1].toUpperCase(); },
            camelCaseToDashSeparated = function(str) { return "-" + str.toLowerCase(); },
            computed = _getComputedStyle(documentElement),
            // In Opera CSSStyleDeclaration objects returned by _getComputedStyle have length 0
            props = computed.length ? _slice(computed) : _map(_keys(computed), function(key) { return key.replace(reCamel, camelCaseToDashSeparated); });
        
        _forEach(props, function(propName) {
            var prefix = propName[0] === "-" ? propName.substr(1, propName.indexOf("-", 1) - 1) : null,
                unprefixedName = prefix ? propName.substr(prefix.length + 2) : propName,
                stylePropName = propName.replace(reDash, dashSeparatedToCamelCase);

            // some browsers start vendor specific props in lowecase
            if (!(stylePropName in computed)) {
                stylePropName = stylePropName[0].toLowerCase() + stylePropName.substr(1);
            }

            if (stylePropName !== propName) {
                getStyleHooks[unprefixedName] = function(style) {
                    return style[stylePropName];
                };

                setStyleHooks[unprefixedName] = function(name, value) {
                    return propName + ":" + value;
                };
            }
        });

        // shortcuts
        _forOwn({
            font: ["fontStyle", "fontSize", "/", "lineHeight", "fontFamily"],
            padding: ["paddingTop", "paddingRight", "paddingBottom", "paddingLeft"],
            margin: ["marginTop", "marginRight", "marginBottom", "marginLeft"],
            "border-width": ["borderTopWidth", "borderRightWidth", "borderBottomWidth", "borderLeftWidth"],
            "border-style": ["borderTopStyle", "borderRightStyle", "borderBottomStyle", "borderLeftStyle"]
        }, function(value, key) {
            getStyleHooks[key] = function(style) {
                var result = [],
                    hasEmptyStyleValue = function(prop, index) {
                        result.push(prop === "/" ? prop : style[prop]);

                        return !result[index];
                    };

                return _some(value, hasEmptyStyleValue) ? "" : result.join(" ");
            };
        });

        // normalize float css property
        if ("cssFloat" in computed) {
            getStyleHooks.float = function(style) {
                return style.cssFloat;
            };
        } else {
            getStyleHooks.float = function(style) {
                return style.styleFloat;
            };
        }
        
        _forEach("fill-opacity font-weight line-height opacity orphans widows z-index zoom".split(" "), function(propName) {
            // Exclude the following css properties to add px
            setStyleHooks[propName] = function(name, value) {
                return name + ":" + value;
            };
        });

        /**
         * Get css style from element
         * @param  {String} name property name
         * @return {String} property value
         */
        $Element.prototype.getStyle = function(name) {
            var style = this._node.style,
                hook, result;

            if (typeof name !== "string") {
                throw _makeError("getStyle", this);
            }

            hook = getStyleHooks[name];

            result = hook ? hook(style) : style[name];

            if (!result) {
                style = _getComputedStyle(this._node);

                result = hook ? hook(style) : style[name];
            }

            return result;
        };

        /**
         * Set css style for element
         * @param {String} name  property name
         * @param {String} value property value
         * @return {$Element}
         */
        $Element.prototype.setStyle = function(name, value) {
            var nameType = typeof name,
                cssText = "", hook;

            if (nameType === "string") {
                hook = setStyleHooks[name];

                cssText = ";" + (hook ? hook(name, value) : name + ":" + (typeof value === "number" ? value + "px" : value));
            } else if (nameType === "object") {
                _forOwn(name, function(value, key) {
                    hook = setStyleHooks[key];

                    cssText += ";" + (hook ? hook(key, value) : key + ":" + (typeof value === "number" ? value + "px" : value));
                });
            } else {
                throw _makeError("setStyle", this);
            }

            this._node.style.cssText += cssText;

            return this;
        };
    })();

    // TRAVERSING
    // ----------
    
    (function() {
        function makeTraversingMethod(propertyName, multiple) {
            return function(selector) {
                var matcher = SelectorMatcher(selector),
                    nodes = multiple ? [] : null,
                    it = this._node;

                while (it = it[propertyName]) {
                    if (it.nodeType === 1 && (!matcher || matcher.test(it))) {
                        if (!multiple) break;

                        nodes.push(it);
                    }
                }

                return multiple ? new $CompositeElement(nodes) : $Element(it);
            };
        }

        function makeChildTraversingMethod(multiple) {
            return function(index, selector) {
                if (multiple) {
                    selector = index;
                } else if (typeof index !== "number") {
                    throw _makeError("child", this);
                }

                var children = this._node.children,
                    matcher = SelectorMatcher(selector),
                    node;

                if (!document.addEventListener) {
                    // fix IE8 bug with children collection
                    children = _filter(children, function(node) { return node.nodeType === 1; });
                }

                if (multiple) {
                    return new $CompositeElement(!matcher ? children : _filter(children, matcher.test, matcher));
                }

                if (index < 0) index = children.length + index;

                node = children[index];

                return $Element(!matcher || matcher.test(node) ? node : null);
            };
        }

        /**
         * Find next sibling element filtered by optional selector
         * @param {String} [selector] css selector
         * @return {$Element} matched element
         * @function
         */
        $Element.prototype.next = makeTraversingMethod("nextSibling");

        /**
         * Find previous sibling element filtered by optional selector
         * @param {String} [selector] css selector
         * @return {$Element} matched element
         * @function
         */
        $Element.prototype.prev = makeTraversingMethod("previousSibling");

        /**
         * Find all next sibling elements filtered by optional selector
         * @param {String} [selector] css selector
         * @return {$CompositeElement} matched elements
         * @function
         */
        $Element.prototype.nextAll = makeTraversingMethod("nextSibling", true);

        /**
         * Find all previous sibling elements filtered by optional selector
         * @param {String} [selector] css selector
         * @return {$CompositeElement} matched elements
         * @function
         */
        $Element.prototype.prevAll = makeTraversingMethod("previousSibling", true);

        /**
         * Find parent element filtered by optional selector
         * @param {String} [selector] css selector
         * @return {$Element} matched element
         * @function
         */
        $Element.prototype.parent = makeTraversingMethod("parentNode");

        /**
         * Return child element by index filtered by optional selector
         * @param  {Number} index child index
         * @param  {String} [selector] css selector
         * @return {$Element} matched child
         * @function
         * @example
         * var body = DOM.find("body");
         *
         * body.child(0); // => first child
         * body.child(-1); // => last child
         */
        $Element.prototype.child = makeChildTraversingMethod(false);

        /**
         * Fetch children elements filtered by optional selector
         * @param  {String} [selector] css selector
         * @return {$CompositeElement} matched elements
         * @function
         */
        $Element.prototype.children = makeChildTraversingMethod(true);
    })();

    /**
     * Prepend extra arguments to the method with specified name
     * @param  {String}    name  name of method to bind arguments with
     * @param  {...Object} args  extra arguments to prepend to the method
     * @return {$Element}
     */
    $Element.prototype.bind = function(name) {
        var self = this,
            args = _slice(arguments, 1),
            method = this[name];

        if (typeof method !== "function" || method in $Node.prototype || method in $Element.prototype) {
            throw _makeError("bind", this);
        }

        this[name] = function() {
            return method.apply(self, args.concat(_slice(arguments)));
        };

        return this;
    };

    /**
     * Show element
     * @return {$Element}
     */
    $Element.prototype.show = function() {
        this.set("hidden", false);

        return this;
    };

    /**
     * Hide element
     * @return {$Element}
     */
    $Element.prototype.hide = function() {
        this.set("hidden", true);

        return this;
    };

    /**
     * Toggle element visibility
     * @return {$Element}
     */
    $Element.prototype.toggle = function() {
        this.set("hidden", !this.get("hidden"));

        return this;
    };

    /**
     * Check is element is hidden
     * @return {Boolean} true if element is hidden
     */
    $Element.prototype.isHidden = function() {
        return !!this.get("hidden");
    };

    /**
     * Check if element has focus
     * @return {Boolean} true if current element is focused
     */
    $Element.prototype.isFocused = function() {
        return this._node === document.activeElement;
    };

    // COMPOSITE ELEMENT
    // -----------------

    /**
     * Array-like collection of elements with the same interface as $Element. Setters do
     * processing for each element, getters return undefined value.
     * @name $CompositeElement
     * @extends $Element
     * @constructor
     * @private
     */
    function $CompositeElement(elements) {
        Array.prototype.push.apply(this, _map(elements, $Element));
    }

    $CompositeElement.prototype = new $Element();
    $CompositeElement.prototype.constructor = $CompositeElement;

    _forIn($CompositeElement.prototype, function(value, key, proto) {
        if (typeof value !== "function") return;

        if (~value.toString().indexOf("return this;")) {
            proto[key] = function() {
                var args = arguments;

                return _forEach(this, function(el) {
                    value.apply(el, args);
                });
            };
        } else {
            proto[key] = function() {};
        }
    });

    // ELEMENT COLLECTION EXTESIONS
    // ----------------------------

    _extend($Element.prototype, {
        /**
         * Executes callback on each element in the collection
         * @memberOf $Element.prototype
         * @param  {Function} callback callback function
         * @param  {Object}   [thisArg]  callback context
         * @return {$Element}
         */
        each: function(callback, thisArg) {
            return _forEach(this, callback, thisArg);
        },

        /**
         * (alias: <b>any</b>) Checks if the callback returns true for any element in the collection
         * @memberOf $Element.prototype
         * @param  {Function} callback   callback function
         * @param  {Object}   [thisArg]  callback context
         * @return {Boolean} true, if any element in the collection return true
         */
        some: function(callback, thisArg) {
            return _some(this, callback, thisArg);
        },

        /**
         * (alias: <b>all</b>) Checks if the callback returns true for all elements in the collection
         * @memberOf $Element.prototype
         * @param  {Function} callback   callback function
         * @param  {Object}   [thisArg]  callback context
         * @return {Boolean} true, if all elements in the collection returns true
         */
        every: function(callback, thisArg) {
            return _every(this, callback, thisArg);
        },

        /**
         * (alias: <b>collect</b>) Creates an array of values by running each element in the collection through the callback
         * @memberOf $Element.prototype
         * @param  {Function} callback   callback function
         * @param  {Object}   [thisArg]  callback context
         * @return {Array} new array of the results of each callback execution
         */
        map: function(callback, thisArg) {
            return _map(this, callback, thisArg);
        },

        /**
         * (alias: <b>select</b>) Examines each element in a collection, returning an array of all elements the callback returns truthy for
         * @memberOf $Element.prototype
         * @param  {Function} callback   callback function
         * @param  {Object}   [thisArg]  callback context
         * @return {$Element} new $CompositeElement of elements that passed the callback check
         */
        filter: function(callback, thisArg) {
            return new $CompositeElement(_filter(this, callback, thisArg));
        },

        /**
         * (alias: <b>foldl</b>) Boils down a list of values into a single value (from start to end)
         * @memberOf $Element.prototype
         * @param  {Function} callback callback function
         * @param  {Object}   memo     initial value of the accumulator
         * @return {Object} the accumulated value
         */
        reduce: function(callback, memo) {
            return _foldl(this, callback, memo);
        },

        /**
         * (alias: <b>foldr</b>) Boils down a list of values into a single value (from end to start)
         * @memberOf $Element.prototype
         * @param  {Function} callback callback function
         * @param  {Object}   memo     initial value of the accumulator
         * @return {Object} the accumulated value
         */
        reduceRight: function(callback, memo) {
            return _foldr(this, callback, memo);
        },

        /**
         * Calls the method named by name on each element in the collection
         * @memberOf $Element.prototype
         * @param  {String}    name   name of the method
         * @param  {...Object} [args] arguments for the method call
         * @return {$Element}
         */
        invoke: function(name) {
            var args = _slice(arguments, 1);

            if (typeof name !== "string") {
                throw _makeError("invoke", this);
            }

            return _forEach(this, function(el) {
                if (args.length) {
                    el[name].apply(el, args);
                } else {
                    el[name]();
                }
            });
        }
    });

    // aliases
    _forOwn({
        all: "every",
        any: "some",
        collect: "map",
        select: "filter",
        foldl: "reduce",
        foldr: "reduceRight"
    }, function(value, key) {
        this[key] = this[value];
    }, $Element.prototype);

    // GLOBAL API
    // ----------

    /**
     * Global object to access DOM
     * @namespace DOM
     * @extends $Node
     */
    var DOM = new $Node(document);

    DOM.version = "1.1.0";

    // WATCH CALLBACK
    // --------------

    /**
     * Execute callback when element with specified selector matches
     * @memberOf DOM
     * @param {String} selector css selector
     * @param {Fuction} callback event handler
     * @param {Boolean} [once] execute callback only at the first time
     * @function
     */
    DOM.watch = (function() {
        var watchers, computed, cssPrefix, scripts, behaviorUrl;

        if (window.CSSKeyframesRule || !document.attachEvent) {
            // Inspired by trick discovered by Daniel Buchner:
            // https://github.com/csuwldcat/SelectorListener
            computed = _getComputedStyle(documentElement);
            cssPrefix = window.CSSKeyframesRule ? "" : (_slice(computed).join().match(/-(moz|webkit)-/) || (computed.OLink === "" && ["-o-"]))[0];
            watchers = {};

            _forEach(["animationstart", "oAnimationStart", "webkitAnimationStart"], function(name) {
                document.addEventListener(name, function(e) {
                    var entry = watchers[e.animationName],
                        node = e.target;

                    if (entry) {
                        // MUST cancelBubbling first otherwise may have extra calls in firefox
                        if (entry.once) node.addEventListener(name, entry.once, false);

                        entry.callback($Element(node));
                    }
                }, false);
            });

            return function(selector, callback, once) {
                var animationName = _uniqueId("DOM"),
                    animations = [animationName];

                _forOwn(watchers, function(entry, key) {
                    if (entry.selector === selector) animations.push(key);
                });

                DOM.importStyles("@" + cssPrefix + "keyframes " + animationName, "1% {opacity: .99}");

                DOM.importStyles(selector, {
                    "animation-duration": "1ms",
                    "animation-name": animations.join() + " !important"
                });

                watchers[animationName] = {
                    selector: selector,
                    callback: callback,
                    once: once && function(e) {
                        if (e.animationName === animationName) e.stopPropagation();
                    }
                };
            };
        } else {
            scripts = document.scripts;
            behaviorUrl = scripts[scripts.length - 1].getAttribute("data-htc");
            watchers = [];

            document.attachEvent("ondataavailable", function() {
                var e = window.event,
                    node = e.srcElement;

                if (e._type === undefined) {
                    _forEach(watchers, function(entry) {
                        // do not execute callback if it was previously excluded
                        if (_some(e.detail, function(x) { return x === entry.callback; })) return;

                        if (entry.matcher.test(node)) {
                            if (entry.once) node.attachEvent("on" + e.type, entry.once);

                            _defer(function() { entry.callback($Element(node)); });
                        }
                    });
                }
            });

            return function(selector, callback, once) {
                var behaviorExists = _some(watchers, function(x) { return x.matcher.selector === selector; });
                
                if (behaviorExists) {
                    // call the callback manually for each matched element
                    // because the behaviour is already attached to selector
                    // also execute the callback safely
                    DOM.findAll(selector).each(function(el) {
                        _defer(function() { callback(el); });
                    });
                }

                watchers.push({
                    callback: callback,
                    matcher: new SelectorMatcher(selector),
                    once: once && function() {
                        var e = window.event;

                        if (e._type === undefined) {
                            (e.detail = e.detail || []).push(callback);
                        }
                    }
                });

                if (!behaviorExists) DOM.importStyles(selector, {behavior: "url(" + behaviorUrl + ")"});
            };
        }
    }());

    // CREATE ELEMENT
    // --------------

    (function(){
        var rquick = /^[a-z]+$/;

        /**
         * Create a $Element instance
         * @memberOf DOM
         * @param  {String|Element} value native element or element tag name
         * @return {$Element} element
         */
        DOM.create = function(value) {
            if (typeof value === "string") {
                if (value.match(rquick)) {
                    value = _createElement(value);
                } else {
                    if (value[0] !== "<") value = DOM.parseTemplate(value);

                    value = _parseFragment(value);
                }
            }

            var nodeType = value.nodeType, div;

            if (nodeType === 11) {
                if (value.childNodes.length === 1) {
                    value = value.firstChild;
                } else {
                    // wrap result with div
                    div = _createElement("div");
                    div.appendChild(value);
                    value = div;
                }
            } else if (nodeType !== 1) {
                this.error("create");
            }

            return $Element(value);
        };
    })();

    /**
     * Define a DOM extension
     * @memberOf DOM
     * @param  {String} selector extension css selector
     * @param  {Array}  [template] extension templates
     * @param  {Object} mixins extension mixins
     * @example
     * DOM.extend(".myplugin", [
     *     "&#60;span&#62;myplugin text&#60;/span&#62;"
     * ], {
     *     constructor: function(tpl) {
     *         // initialize extension
     *     }
     * });
     *
     * // emmet-like syntax example
     * DOM.extend(".mycalendar", [
     *     "table>(tr>th*7)+(tr>td*7)*6"
     * ], {
     *     constructor: function(tpl) {
     *         // initialize extension
     *     },
     *     method: function() {
     *         // this method will be mixed into every instance
     *     }
     * });
     */
    DOM.extend = function(selector, template, mixins) {
        if (mixins === undefined) {
            mixins = template;
            template = undefined;
        }

        if (typeof mixins === "function") {
            mixins = {constructor: mixins};
        }

        if (!mixins || typeof mixins !== "object" || (selector !== "*" && ~selector.indexOf("*"))) {
            throw _makeError("extend", this);
        }

        if (selector === "*") {
            // extending element prototype
            _extend($Element.prototype, mixins);
        } else {
            template = _map(template || [], DOM.create);
            // update internal element mixins
            DOM.mock(selector, mixins);

            DOM.watch(selector, function(el) {
                _extend(el, mixins);

                if (mixins.hasOwnProperty("constructor")) {
                    mixins.constructor.apply(el, _map(template, function(value) {
                        return value.clone();
                    }));
                }
            }, true);
        }
    };

    // EMMET EXPRESSIONS PARSER
    // ------------------------

    (function() {
        // operator type / priority object
        var operators = {"(": 1,")": 2,"^": 3,">": 4,"+": 4,"*": 5,"}": 5,"{": 6,"]": 5,"[": 6,".": 7,"#": 8,":": 9},
            emptyElements = " area base br col hr img input link meta param command keygen source ",
            reEmpty = /<\?>|<\/\?>/g,
            reAttr = /([\w\-]+)(?:=((?:"((?:\\.|[^"])*)")|(?:'((?:\\.|[^'])*)')|([^\s\]]+)))?/g,
            reIndex = /(\$+)(?:@(-)?([0-9]+)?)?/,
            reIndexg = new RegExp(reIndex.source, "g"),
            normalizeAttrs = function(term, name, value, a, b, simple) {
                // always wrap attribute values with quotes if they don't exist
                return name + "=" + (simple || !value ? "\"" + (value || "") + "\"" : value);
            },
            formatIndex = function(index) {
                return function(expr, fmt) {
                    return (fmt + index).slice(-fmt.length).split("$").join("0");
                };
            };

        // helper class
        function HtmlBuilder(node, n) {
            if (n) {
                var parsed = reIndex.exec(node) || [],
                    step = parsed[2] ? -1 : 1,
                    i = parsed[3] ? +parsed[3] : 1;

                if (step < 0) i += n - 1;

                for (; n--; i += step) {
                    this.push(node.replace(reIndexg, formatIndex(i)));
                }
            } else {
                this.push(HtmlBuilder.parse(node));
            }
        }

        HtmlBuilder.parse = function(term) {
            var result = "<" + term + ">";

            if (emptyElements.indexOf(" " + term + " ") < 0) {
                result += "</" + term + ">";
            }

            return result;
        };

        HtmlBuilder.prototype = {
            push: Array.prototype.push,
            inject: function(term, first) {
                for (var i = 0, n = this.length, index, el; i < n; ++i) {
                    el = this[i];
                    index = first ? el.indexOf(">") : el.lastIndexOf("<");
                    // inject term into the html string
                    this[i] = el.substr(0, index) + term + el.substr(index);
                }
            },
            toString: function() {
                return Array.prototype.join.call(this, "");
            }
        };

        /**
         * Parse emmet-like template to HTML string
         * @memberOf DOM
         * @param  {String} template emmet-like expression
         * @return {String} HTML string
         * @see http://docs.emmet.io/cheat-sheet/
         */
        DOM.parseTemplate = function(template) {
            var stack = [],
                output = [],
                term = "",
                i, n, str, priority, skip, node;

            // parse exrpression into RPN
            
            for (i = 0, n = template.length; i < n; ++i) {
                str = template[i];
                // concat .c1.c2 into single space separated class string
                if (str === "." && stack[0] === ".") str = " ";

                priority = operators[str];

                if (priority && (!skip || skip === str)) {
                    // append empty tag for text nodes or put missing '>' operator into the stack
                    if (str === "{") {
                        if (term) {
                            stack.unshift(">");
                        } else {
                            term = "?";
                        }
                    }
                    // remove redundat ^ operators from the stack when more than one exists
                    if (str === "^" && stack[0] === "^") stack.shift();

                    if (term) {
                        output.push(term);
                        term = "";
                    }

                    if (str !== "(") {
                        while (operators[stack[0]] > priority) {
                            output.push(stack.shift());
                            // for ^ operator stop shifting when the first > is found
                            if (str === "^" && output[output.length - 1] === ">") break;
                        }
                    }

                    if (str === ")") {
                        stack.shift(); // remove "(" symbol from stack
                    } else if (!skip) {
                        stack.unshift(str);

                        if (str === "[") skip = "]";
                        if (str === "{") skip = "}";
                    } else {
                        skip = false;
                    }
                } else {
                    term += str;
                }
            }

            if (term) stack.unshift(term);

            output.push.apply(output, stack);

            // transform RPN into html nodes

            stack = [];

            if (output.length === 1) output.push(">");

            for (i = 0, n = output.length; i < n; ++i) {
                str = output[i];

                if (str in operators) {
                    term = stack.shift();
                    node = stack.shift() || "?";

                    if (typeof node === "string") node = new HtmlBuilder(node);

                    switch(str) {
                    case ".":
                        node.inject(" class=\"" + term + "\"", true);
                        break;

                    case "#":
                        node.inject(" id=\"" + term + "\"", true);
                        break;

                    case ":":
                        node.inject(" type=\"" + term + "\"", true);
                        break;

                    case "[":
                        node.inject(" " + term.replace(reAttr, normalizeAttrs), true);
                        break;

                    case "{":
                        node.inject(term);
                        break;

                    case "*":
                        node = new HtmlBuilder(node.toString(), parseInt(term, 10));
                        break;

                    default:
                        term = typeof term === "string" ? HtmlBuilder.parse(term) : term.toString();

                        node[str === ">" ? "inject" : "push"](term);
                        break;
                    }

                    str = node;
                }

                stack.unshift(str);
            }

            return stack[0].toString().replace(reEmpty, "");
        };
    })();

    // IMPORT STYLES
    // -------------

    (function() {
        var styleNode = documentElement.firstChild.appendChild(_createElement("style")),
            styleSheet = styleNode.sheet || styleNode.styleSheet;

        /**
         * Import global css styles on page
         * @memberOf DOM
         * @param {String|Object} selector css selector or object with selector/rules pairs
         * @param {String} styles css rules
         */
        DOM.importStyles = function(selector, styles) {
            if (typeof styles === "object") {
                var obj = {_node: {style: {cssText: ""}}};

                $Element.prototype.setStyle.call(obj, styles);

                styles = obj._node.style.cssText.substr(1); // remove leading comma
            }

            if (typeof selector !== "string" || typeof styles !== "string") {
                throw _makeError("importStyles", this);
            }

            if (styleSheet.cssRules) {
                styleSheet.insertRule(selector + " {" + styles + "}", styleSheet.cssRules.length);
            } else {
                // ie doesn't support multiple selectors in addRule
                _forEach(selector.split(","), function(selector) {
                    styleSheet.addRule(selector, styles);
                });
            }
        };

        if (!DOM.supports("hidden", "a")) {
            DOM.importStyles("[hidden]", "display:none");
        }
    }());

    // READY CALLBACK
    // --------------

    (function() {
        var readyCallbacks = [],
            readyState = document.readyState,
            pageLoaded = function() {
                if (readyCallbacks) {
                    // safely trigger callbacks
                    _forEach(readyCallbacks, _defer);
                    // cleanup
                    readyCallbacks = null;
                }
            };

        if (document.addEventListener) {
            document.addEventListener("DOMContentLoaded", pageLoaded, false);
            window.addEventListener("load", pageLoaded, false);
        } else {
            DOM.watch("body", pageLoaded, true);
        }

        // Catch cases where ready is called after the browser event has already occurred.
        // IE10 and lower don't handle "interactive" properly... use a weak inference to detect it
        // discovered by ChrisS here: http://bugs.jquery.com/ticket/12282#comment:15
        if (document.attachEvent ? readyState === "complete" : readyState !== "loading") {
            pageLoaded();
        }

        /**
         * Execute callback when DOM will be ready
         * @memberOf DOM
         * @param {Function} callback event listener
         */
        DOM.ready = function(callback) {
            if (typeof callback !== "function") {
                throw _makeError("ready", this);
            }

            if (readyCallbacks) {
                readyCallbacks.push(callback);
            } else {
                _defer(callback);
            }
        };
    })();

    (function() {
        var extensions = {};

        /**
         * Return an {@link $Element} mock specified for optional selector
         * @memberOf DOM
         * @param  {String} [selector] selector of mock
         * @return {$Element} mock instance
         */
        DOM.mock = function(selector, mixins) {
            if (selector && typeof selector !== "string" || mixins && typeof mixins !== "object") {
                throw _makeError("mock", this);
            }

            if (!mixins) {
                var el = new $CompositeElement();

                if (selector) {
                    _extend(el, extensions[selector]);

                    el.constructor = $CompositeElement;
                }

                return el;
            }

            extensions[selector] = _extend(extensions[selector] || {}, mixins);
        };
    })();

    // IMPORT STRINGS
    // --------------

    /**
     * Import global i18n string(s)
     * @memberOf DOM
     * @param {String|Object}  key     string key
     * @param {String}         pattern string pattern
     * @param {String}         [lang]  string language
     * @function
     * @example
     * // have element &#60;a data-i18n="str.1" data-user="Maksim"&#62;&#60;a&#62; in markup
     * DOM.importStrings("str.1", "Hello {user}!");
     * DOM.importStrings("str.1", "Привет!", "ru");
     * // the link text now is "Hello Maksim!"
     * link.set("lang", "ru");
     * // the link text now is "Привет!"
     */
    DOM.importStrings = (function() {
        var rparam = /\{([a-z\-]+)\}/g,
            toContentAttr = function(term, attr) { return "\"attr(data-" + attr + ")\""; };

        return function(key, pattern, lang) {
            var keyType = typeof key,
                selector, content;

            if (keyType === "string") {
                selector = "[data-i18n=\"" + key + "\"]";
                
                if (lang) selector += ":lang(" + lang + ")";

                content = "content:\"" + pattern.replace(rparam, toContentAttr) + "\"";

                DOM.importStyles(selector + ":before", content);
            } else if (keyType === "object") {
                lang = pattern;

                _forOwn(key, function(pattern, key) {
                    DOM.importStrings(key, pattern, lang);
                });
            } else {
                throw _makeError("importStrings", this);
            }
        };
    }());

    DOM.importStyles("[data-i18n]:before", "content:'???'attr(data-i18n)'???'");

    /**
     * Return current page title
     * @memberOf DOM
     * @return {String} current page title
     */
    DOM.getTitle = function() {
        return document.title;
    };

    /**
     * Change current page title
     * @memberOf DOM
     * @param  {String} value new title
     * @return {DOM}
     */
    DOM.setTitle = function(value) {
        if (typeof value !== "string") {
            throw _makeError("setTitle", this);
        }
        
        document.title = value;

        return this;
    };

    // REGISTER API
    // ------------

    window.DOM = DOM;
    
    if (typeof define === "function" && define.amd) {
        define("DOM", function() { return DOM; });
    }
    
})(window, document, document.documentElement);
