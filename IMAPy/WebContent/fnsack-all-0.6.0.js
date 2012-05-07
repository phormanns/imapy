/*
 *  FunctionSack v 0.5.0
 *  http://functionsack.com
 *
 *  Copyright (C) 2011 David de Rosier
 *
 *  Licensed under the MIT license.
 *  http://www.opensource.org/licenses/mit-license.php
 */

/**
 * @fileOverview
 * FunctionSack is a generic-purpose JavaScript library which provides a wide
 * number of functions for browser and server-side developers.
 * It can be loaded via script tag in a HTML page, using require function
 * of CommonJS-compliant engines and with load function of Rhino or 
 * SpiderMonkey. 
 *
 * @author David de Rosier
 * @version 0.6.0
 */
 
 
/**
 * @name fs 
 * @namespace
 * @static
 */
(function(__global, undefined){

    "use strict";
    
    /**
     * Private utils. Set of common functions used by all modules
     * @private
     * @static
     */
    var __utils = {
    
        mixin: function(dst,src1) {
            var src, prop;
            for(var srcId=1, len=arguments.length; srcId < len; ++srcId) {
                for(prop in (src=arguments[srcId])) { if( src.hasOwnProperty(prop) ){
                    dst[prop] = src[prop];
                }}
            }
            return dst;
        },
        
        isString: function(obj) {
            return Object.prototype.toString.call(obj) === "[object String]";
        },
        
        isFunction: function(obj) {
            return Object.prototype.toString.call(obj) === "[object Function]";
        },
        
        isObject: function(obj, considerFunction, considerNull) {
            if( obj === null ) {
                return !!considerNull;
            } else if( Object.prototype.toString.call(obj) === '[object Function]' ) {
                return typeof considerFunction==="undefined" ? true : !!considerFunction;
            }
            return typeof obj === 'object' || Object.prototype.toString.call(obj) === "[object RegExp]";
        },
        
        isArray: function(obj){
            return Array.isArray 
                ? Array.isArray(obj) 
                : Object.prototype.toString.call(obj) === "[object Array]";         
        },
        
        isBasic: function(obj){
            return '[object String][object Number][object Boolean]'
                    .indexOf( Object.prototype.toString.call(obj) ) >= 0;
        }
    };
    
    /**
     * Local name for main namespace object (fs).
     * Result object of FunctionSack factory
     * @private
     */
    var $NS = {};
    
// fs.core module
(function(){
    

    
    
    __utils.mixin( $NS, {
        
            /**
             * @name fs.mixin
             * @function
             * @info Combines multiple objects into one.
             * 
             * @param {Object} dst destination object    
             * @param {Object} src source object (multiple source object allowed)
             * @returns {Object} dst object
             */
            mixin: __utils.mixin,


            /**
             * Converts an object to an array
             * @name fs.toArray
             * @info Converts given object to an array.
             * @function
             * 
             * @param {Object} obj
             * @param {number} [idxFrom]
             * @param {number} [idxTo]
             * @returns {Array}
             */
            toArray: (function(){
                if( Array.prototype.slice.call("a")[0] === "a" && [1].slice(void 0, void 0).length === 1 ){
                    // modern browsers
                    return function(obj, idxFrom, idxTo){
                        if( obj == null ) {
                            return [];
                        }
                        return !__utils.isString(obj) && 'length' in Object(obj) 
                                ? Array.prototype.slice.call(obj, idxFrom, idxTo)
                                : [obj].slice(idxFrom, idxTo);
                    };
                }
                
                // older environments
                return function(obj, idxFrom, idxTo){
                    var args = []; 
                    arguments[1] && args.push(arguments[1]) && arguments[2] && args.push(arguments[2]);
                    if( obj == null ) { // obj or undefined
                        return [];
                    }
                    if( __utils.isString(obj) || !('length' in Object(obj)) ) {
                        obj = [obj];
                    }
                    try {
                        return Array.prototype.slice.apply(obj, args);
                    } catch(ex) {
                        var len = obj.length;
                        var arr = new Array( len );
                        len = args[1] || len;
                        for( var i=args[0] || 0; i < len; ++i ) {
                            arr[i] = obj[i];
                        }
                        return arr;
                    }
                };
            })(),


            /**
             * Binds the function to a given context and returns a wrapper function.
             * Practically it 'converts' a method to a function with remembering 
             * the context.
             * @name fs.bind
             * @info Binds the function to a given context and returns a wrapper function.
             * @function
             * 
             * @param {function} fn Original function 
             * @param {Object} ctx method's context
             * @returns {function} wrapped function
             * 
             * @example var flatFunction = $NS.bind(myFunction, obj);
             * var obj = {
             *    title: "My person title",
             *    showTitle: function(){
             *       alert( this.title );
             *    }
             * };
             * document.addEventListener("click",
             *    $NS.bind( obj.showTitle, document ),
             *    false);
             */
            bind: function(fn, ctx){
                if( !__utils.isFunction(fn) ) {
                    throw new TypeError( "'this' is not a function" );
                }
                var args = Array.prototype.slice.call(arguments,2);
                    
                return function() {
                    return fn.apply( ctx, args.concat(Array.prototype.slice.call(arguments)) );
                };
            }



    
        });
    
})();


/**
 * @name fs.type
 * @static 
 * @namespace Module providing functions for type recognition and manipulation.
 */ 
$NS.type = (function(){


    
    return {
        
        /**
         * Safe version of typeof operator. Differences
         * 1. Does not differenciate between primitives and object wrappers (string and String)
         * 2. Never returns "function" for RegExp (Chrome/V8 case)
         * 3. For null value returns "null" string (unless second parameter specified)
         *
         * @name fs.type.typeOf
         * @info Returns type name of given object
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @param {boolean} [nullAsObject=false] Flag which specifies if null should be considered as object (as it is in typeof operator) 
         * @returns {string} type name of the given object
         * 
         * @example $NS.type.typeOf("123") === "string"
         * @example $NS.type.typeOf(new String("123")) === "string"
         * @example $NS.type.typeOf(/.+/g) === 'object'
         * @example $NS.type.typeOf(null) === 'null'
         * @example $NS.type.typeOf(null, true) === 'object'
         */
        typeOf: function(obj, nullAsObject) {
            if( obj === null ) {
                return !!nullAsObject ? "object" : "null";
            }
            var type = typeof obj;
            if( type === "function" ) {
                return this.isRegExp(obj) ? "object" : type;
            } else if( type === "object" ) {
                type = (Object.prototype.toString.call(obj).match(/\s(\S+?)(?=\]$)/) || [])[1];
                return new RegExp("\\b"+type+"\\b").test("String Boolean Number") ? type.toLowerCase() : "object";
            }
            return type;
        },


        /**
         * Returns the value of Class internal property of a given object. The Class internal property
         * is a string representing the name of object's constructor. According to ECMAScript 
         * specification only the standard, built-in objects provide the value of Class property. For all
         * other objects (like custom types) the returned value will be always "Object" - however
         * it might be implementation-dependent. In example DOM objects in Firefox browser
         * provide custom values of Class properties, so getClass(document) will return "HTMLDocument". 
         * The same example on Internet Explorer will return "Object". 
         * To get constructor name instead of "Object" for custom types use getName function instead.
         * 
         * @name fs.type.getClassName
         * @info Returns value of the [[Class]] internal property of an object
         * @function
         * @nosideeffects
         * @see $NS.type.getConstructorName
         * 
         * @param {*} obj
         * @returns {string} value of ECMAScript [[Class]] internal property
         * 
         * @example $NS.type.getClass({}) === 'Object';
         * @example $NS.type.getClass([]) === 'Array';
         * @example $NS.type.getClass() === "Undefined"
         */
        getClassName: function(obj) {
            if( obj === null ) {
                return "Null";
            }
            if( typeof obj === "undefined" ) {
                return "Undefined";
            }
            return (Object.prototype.toString.call(obj).match(/\s(\S+?)(?=\]$)/) || [""])[1];
        },


        /**
         * Returns a type name of an object passed as an argument. It works with
         * primitives and objects. For primitives the function returns value of
         * typeof operator. For objects the function uses following algorithm:
         * 1. if object is null return "null"
         * 2. if object is a built-in object then return its [[Class]] internal property
         *    (ie. "Array" for array object)
         * 3. if object.constructor.name is defined and not empty then return it as a result
         * 4. try to convert constructor function to a string and recognize its name
         * 5. if there is not possible to determine type name, return "Object*" string
         * 
         * @name fs.type.getConstructorName
         * @info Replaces typeof and instanceof to one consistent solution
         * @nosideeffects
         * @requires fs.type.getClassName
         * @see $NS.type.getClassName
         * @function
         * 
         * @param {Object} obj an object to test; can be of any type
         * @param {string} [unrecognizedConstructorName="Object*"] a name returned when function
         *                     won't be able to recognize the constructor name.
         * @returns {string} type name
         * 
         * @example $NS.type.getName(123) === "number"
         * @example $NS.type.getName(new Number(1)) === "Number"
         * @example $NS.type.getName([]} === "Array" 
         * @example $NS.type.getName(function(){}) === "function"
         * @example $NS.type.getName(/.+/) === "RegExp"
         * @example $NS.type.getName(null) === "null"
         * @example $NS.type.getName() === "undefined"
         */
        getConstructorName: function(obj, unrecognizedConstructorName) {
            if( obj === null ) {
                return "null";
            }
            var t = typeof obj;
            
            // 'function' not returned here due to RegExp case on V8
            if( t !== 'object' && t !== 'function' ) {
                return t;
            }
            
            t=$NS.type.getClassName(obj);
            // built-in objects case
            if( t !== 'Object' || obj.constructor === Object ) {
                return t;
            }
            
            // when type name provided via constructor
            // (can return wrong values for overridden constructors)
            if( obj.constructor && obj.constructor.name ) {
                return obj.constructor.name;
            }
            
            // Find function name in the constructors' source 
            if( obj.constructor && (t=String(obj.constructor).match(/^function\s+([^\s\(]+?)[\(\s]/)) !== null ) {
                return t[1];
            }

            // when not possible to determine object's type name
            return unrecognizedConstructorName || "Object*";
        },


        /**
         * Check weather passed parameter is a function.
         * Usage of this method is more secure than typeof operator, because in some
         * environments (like Google Chrome / V8), typeof can return "function" value also
         * for non-function arguments, like regular expressions
         * 
         * @name fs.type.isFunction
         * @info Check weather passed parameter is a function.
         * @nosideeffects
         * @function
         *  
         * @param {*} obj An object to examine
         * @returns {boolean} true if obj is a function, otherwise false
         * 
         * @example ddr.type.isFunction(/.+/) === false
         */
        isFunction: __utils.isFunction,


        /**
         * Checks whether passed argument is an an object.
         * Because in JavaScript function is an object too, the method returns
         * true for functions as well.
         * 
         * @name fs.type.isObject
         * @info Checks whether passed argument is an an object.
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine 
         * @param {boolean} [considerFunction=true] specify if function should be considered as an object
         * @param {boolean} [considerNull=false] specify if null should be considered as an object
         * @returns {boolean} true whether the argument is an object, false otherwise.
         */
        isObject: __utils.isObject,


        /**
         * Checks whether passed argument is an an array.
         * 
         * @name fs.type.isArray
         * @info Checks whether passed argument is an an array.
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} True when obj is an array, false otherwise. 
         */
        isArray: __utils.isArray,


        /**
         * Function checks whether the given attribute is a string. It works
         * correctly both with string primitive and string objects.
         * Thanks to it there is no need in comparisons to call code like 
         * if( typeof x==='string' && x instaceof String) or similar
         * 
         * @name fs.type.isString
         * @info Function checks whether the given attribute is a string.
         * @nosideeffects
         * @function 
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true when obj is string (object or primitive); false otherwise
         */
        isString: __utils.isString,


        /**
         * Function checks whether the given attribute is a number or not. It works
         * with number primitives and object wrappers. 
         * 
         * @name fs.type.isNumber
         * @info Function checks whether the given attribute is a number
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true if obj is a number (or instance of Number); false otherwise
         */
        isNumber: function(obj) {
            return Object.prototype.toString.call(obj) === "[object Number]";
        },


        /**
         * Function checks whether the given attribute is a boolean. It works
         * with boolean primitives and object wrappers. 
         * 
         * @name fs.type.isBoolean
         * @info Function checks whether the given attribute is a boolean
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true if obj is a boolean (or instance of Boolean); false otherwise
         */
        isBoolean: function(obj) {
            return Object.prototype.toString.call(obj) === "[object Boolean]";
        },


        /**
         * Function checks whether the given attribute is a RegExp.
         *  
         * @name fs.type.isRegExp
         * @info Function checks whether the given attribute is a RegExp
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true if obj is a RegExp; false otherwise
         */
        isRegExp: function(obj) {
            return Object.prototype.toString.call(obj) === "[object RegExp]";
        },


        /**
         * Function checks whether the given attribute is a Data object.
         *  
         * @name fs.type.isDate
         * @info Function checks whether the given attribute is a Date object
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true if obj is a Date; false otherwise
         */
        isDate: function(obj) {
            return Object.prototype.toString.call(obj) === "[object Date]";
        },


        /**
         * Function checks whether the given attribute is an instance of
         * Error object. It considers objects created with Error constructor or
         * inheriting from Error's prototype (like TypeError) 
         * 
         * @name fs.type.isError
         * @info Function checks whether the given attribute is instance of Error
         * @nosideeffects
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true if obj is an Error instance; false otherwise
         */
        isError: function(obj) {
            return Object.prototype.toString.call(obj) === "[object Error]";
        },


        /**
         * Test if passed object is a global object. If the second attribute is set to true
         * the function will consider only the current global object.
         *  
         * @name fs.type.isGlobal
         * @info Function checks whether given attribute is a reference to the global object
         * @nosideeffects 
         * @function
         * 
         * @param {*} obj obj An object to examine
         * @param {boolean} [currentContextOnly=true]
         * @returns {boolean}
         */
        isGlobal: function(obj, currentContextOnly) {
            if( !obj || typeof obj !== 'object' ) {
                return false;
            }
            // non strict mode 
            if( (function(){ return this; })() === obj ) {
                return true;
            }
            if( typeof currentContextOnly === "undefined" || !!currentContextOnly ) {
                return Function===obj.Function && encodeURIComponent===obj.encodeURIComponent 
                        && RegExp===obj.RegExp && isNaN === obj.isNaN; 
            }
            // Rhino, Node.JS
            if( Object.prototype.toString.call(obj) === "[object Error]" ) {
                return true;
            }
            return obj.Function && obj.RegExp && obj.encodeURIComponent && obj.RegExp && "undefined" in obj;
        },


        /**
         * Returns true if the argument is a falsy value, so one of
         * the following values: 0, "", false, null, undefined or... the object
         * wrapper of them. The last one may sound bit controversial, however
         * it matches the way how FunctionSack treats the types. For more details
         * see the description of type modele. 
         * @name fs.type.isFalsy
         * @info Returns true if the argument is a falsy value.
         * @see $NS.type
         * @requires fs.type.isWrapper 
         * @function  
         * @param {*} obj A value to test
         * @returns {boolean} true for falsy value; false otherwise
         */
        isFalsy: function(obj) {
            return !obj || (this.isWrapper(obj) && !obj.valueOf());
        },


        /**
         * @name fs.type.isStandardType
         * @info Checks if given object is a standard type
         * @requires fs.type.getClassName
         * @function
         * 
         * @param {*} An object to examine
         * @returns {boolean} 
         */
        isStandardType: function(obj) {
            if( typeof obj !== "object" || obj === null ) {
                return true;
            }
            var c = this.getClassName(obj);
            return c === "String" || c === "Number" || c === "Boolean" || c === "Date" 
                    || c === "RegExp" || c === "Error" || c === "Array";
        },


        /**
         * @name fs.type.isWrapper
         * @info Checks if given object is an object representation of a primitive type.
         * @requires fs.type.getClassName
         * @function
         */
        isWrapper: function(obj) {
            var c;
            return __utils.isObject(obj,false) 
                        && ((c=this.getClassName(obj)) === "String" || c === "Number" || c === "Boolean");
        },


        /**
         * Checks if given object is a basic type (number, string, boolean) or its wrapper
         * @name fs.type.isBasic
         * @info Checks if given object is a basic type (number, string, boolean)
         * @function
         * @param {*} obj Object to check
         * @returns {boolean} 
         * @example $NS.type.isBasic(3) === true;
         * @example $NS.type.isBasic(new Number(3)) === true;
         * @example $NS.type.isBasic({}) === false; 
         */
        isBasic: __utils.isBasic




    };
    
})();

    


/**
 * @name fs.obj
 * @namespace Provides object access and manipulation functions.
 * @static
 */ 
$NS.obj = (function(){

    var __throwTypeErrorForNonObject = function(obj){
        if( !__utils.isObject(obj) ) {
            throw new TypeError( obj+" is not an object" );
        }
    };


    
    return {
        
        /**
         * Allows to get an access to deep object properties via string.
         * When given path is not reachable function return undefined or 
         * default value. 
         * 
         * @name fs.obj.access
         * @info Access deep object properties with a string path. 
         * @requires Array.prototype.reduce
         * @function
         * 
         * @param {Object} ctx Object to examine
         * @param {string} path Property path
         * @param {*} [defaultValue=undefined] Default value
         * @returns {*} Value of the property selected by path or default value 
         * 
         * @example
         * var obj = {
         *        name: "test",
         *        addresses: [
         *          { country: "Iceland" },
         *          { street: "Queensway", country: "UK", city: "London" }
         *        ]
         *    };
         * fs.obj.access(obj, "name") === "test";
         * fs.obj.access(obj, "addresses[0].country") === "Iceland";
         * fs.obj.access(obj, "addresses.[0].city.district") === undefined;
         * fs.obj.access(obj, "addresses.1.city") === "London";
         * 
         */
        access: function(ctx, path, defaultValue){
            var value = String(path).split(".").reduce(function fn(obj, pelem){
                    if(obj && pelem.charAt(pelem.length-1)===']'){
                        // can't split with /\[(.+?)\]/ (without replace) due to bug in IE
                        return pelem.replace("]","").split("[")
                                .reduce(fn, obj);
                    }
                    return obj && pelem ? obj[pelem] : obj;
                }, ctx);
            return typeof value !== "undefined" ? value : defaultValue;
        },


        /**
         * Clones the given object with respect to the prototype
         * chain. If object is a clone of obj then it prototypaly 
         * inherits from his origin (obj). It means that obj becomes
         * a prototype for all their clones. 
         * 
         * @name fs.obj.clone
         * @info Creates a clone of an object (ECMAScript 5 Object.create equivalent)
         * @function 
         * 
         * @param {Object} obj Object to clone
         * @returns {Object} cloned object
         * 
         * @example
         * var obj = { x: 1, y: 2 };
         * var clone = fs.obj.clone(obj);
         * // cloned object has properties of its origin... 
         * clone.x === 1;
         * // but their are not clone's internal properties
         * clone.hasOwnProperty("x") === false;
         * // original object is clone's prototype
         * Object.getPrototypeOf(clone) === obj;
         * // and both objects are of the same type
         * clone.constructor === obj.constructor;
         */
        clone: (function(){
            
            var __CloneConstructor = function(){};
            
            return function(obj){
                if( !__utils.isObject(obj) ) {
                    return ob;
                }
                __CloneConstructor.prototype = obj;
                return new __CloneConstructor();
            };
        })(),


        /**
         * Compares two objects by value. By default it does the shallow comparison. It means
         * the internal objects will be compared by references. When third attribute is set to true, 
         * function will run deep comparison - for each internal object it will execute the
         * comparator again recursively - unless it will be able to compare attributes by value.
         * Because the language does not provide standard by-value comparators, certain assumptions
         * had to be taken:<ul><li>
         * a) Standard object wrappers (String, Boolean, Number) will be compared by valueOf method result</li><li>
         * b) Error and RegExp objects will be compared by toString method result</li><li>
         * c) Date object will be compared by getTime method results</li><li>
         * d) all other objects (including arrays) will be compared item by item; applying recursive comparison
         *    for object members</li><li>
         * e) only own, enumerable properties will be compared</li><li>
         * f) objects containing non-enumerable length internal property (like arrays, strings, functions, node lists)   
         *    will be additionally compared by the length value</li><li>
         * g) objects containing different number of internal properties are considered as not equal</li><li>
         * h) functions are compared by length property and by function-object properties; function body</li><li>
         *    is not a subject of comparison as there is no cross-environment solution for such operation</li></ul>
         * 
         * @name fs.obj.equals
         * @info Compares two objects by value (shallow and deep compare possible)
         * @requires Array.prototype.indexOf
         * @requires fs.obj.getLength
         * @requires fs.type.isStandardType
         * @function
         * 
         * @param {*} obj1 First object for comparison
         * @param {*} obj2 Second object for comparison
         * @param {boolean} [deepCompare=false]
         * @returns {boolean} True when objects are equal, false otherwise
         * 
         * @example
         * var obj1 = { x: 1, y: [1,2,3] };
         * var obj2 = { x: 1, y: [1,2,3] };
         * 
         * // shallow comparison returns false because inner arrays are compared by reference
         * $NS.obj.equals(obj1, obj2) === false;
         * 
         * // deep comparison returns true (internal array is compared element by element) 
         * $NS.obj.equals(obj1, obj2, true) === true;
         */
        equals: (function(){
            
            // standard objects comparator
            var __compareStandardOjects = function(v1, v2) {
                var v1t = Object.prototype.toString.call(v1);
                if( v1t !== Object.prototype.toString.call(v2) ) {
                    return false;
                }
                return v1t === "[object RegExp]" || v1t === "[object Error]" 
                            ? v1.toString() === v2.toString() 
                            : v1.valueOf() === v2.valueOf();
            };

            // hidden implementation due to fourth attribute (history)
            var __deepEquals = function (obj1, obj2, deepCompare, history) {
                if( deepCompare && $NS.type.isStandardType(obj1) && !__utils.isFunction(obj1) && !__utils.isArray(obj1) ) {
                    if(__compareStandardOjects(obj1, obj2) === false ) {
                        return false;
                    }
                }     
                if( (Object.keys(obj1).length !== Object.keys(obj2).length) || ("length" in obj1 && obj1.length !== obj2.length) ) {
                    return false;
                }
                for(var key in obj1) {
                    if(obj1.hasOwnProperty(key)){
                        if( !obj2.hasOwnProperty(key) ) {
                            return false;
                        }
                        if( deepCompare && __utils.isObject(obj1[key], true) && __utils.isObject(obj2[key], true) ){
                            if(history.indexOf(obj1[key]) >=0 ) {
                                continue;
                            }
                            history.push(obj1);
                            if( __deepEquals(obj1[key], obj2[key], deepCompare, history) === false ){
                                return false;
                            }
                        } else {
                            if( obj1[key] !== obj2[key] ) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            };
            
            return function(obj1, obj2, deepCompare){
                if( !__utils.isObject(obj1,!!deepCompare) || !__utils.isObject(obj2,!!deepCompare) ) {
                    return obj1 === obj2;
                } 
                return __deepEquals(obj1, obj2, !!deepCompare, []);
            };

        })(),


        /**
         * Enforces execution of $NS.obj.equals function with deepCompare attribute set to true.
         * 
         * @name fs.obj.deepEqual
         * @info Enforces execution of $NS.obj.equals function with deepCompare attribute set to true
         * @see $NS.obj.equals
         * @requires fs.obj.equals
         * @function
         * 
         * @param {*} obj1 First object for comparison
         * @param {*} obj2 Second object for comparison
         * @returns {boolean} comparison result
         * 
         * @example $NS.obj.equals(obj1, obj2, true) === $NS.obj.deepEqual(obj1, obj2);
         */
        deepEqual: function(obj1, obj2) {
            return this.equals(obj1, obj2, true);
        },


        /**
         * Returns an array containing property names of an object filtered by
         * given criteria. Function works similiar to ECMAScript 5 Array.prototype.filter,
         * but the callback attributes are adopted for property selection.
         * The function takes as a parameter an object and a callback function.
         * The callback is executed for each own property of an object. The result of the
         * function is an array of property names for which the callback returned true.  
         * 
         * @name fs.obj.filterPropertyNames
         * @info Filters property names from an object with callback method.
         * @requires Object.getOwnPropertyNames
         * @requires Object.getOwnPropertyDescriptor
         * @requires fs.type.typeOf
         * @requires Array.prototype.filter
         * @see $NS.obj.filter
         * @function
         * 
         * @param {Object} obj Object to filter
         * @param {function} callback A callback function - will be executed for each object's property
         * @param {Object} [thisObj=obj] A value of this property inside a callback
         * @param {boolean} [includeNotEnumerable=false] - flag which says that callback should be run also for non-enumerable properties
         * @returns {Array.<string>}
         * 
         * @example
         * var person = {
         *       firstname: "John",
         *       lastname: "Ubot",
         *       getFullName: function(){ return this.firstname+" "+this.lastname },
         *       age: 42,
         *       sex: "M"
         *    };
         *    
         * // selects from person the property names which point to
         * // non-functions and which contain string "name"
         * // result: ["firstname", "lastname"] 
         * var props = fs.obj.filterPropertyNames(person, function(name, type, desc){
         *       return type !== "function" && /name/.test(name);
         *    });
         */
        filterPropertyNames: function(obj, callback, thisObj, includeNotEnumerable) {
            __throwTypeErrorForNonObject( obj );
            var keys;
            if( !!includeNotEnumerable && Object.getOwnPropertyNames ) {
                keys =  Object.getOwnPropertyNames(obj);
            } else {
                keys = [];
                for(var key in obj) {
                    if( Object.prototype.hasOwnProperty.call(obj, key) ) {
                        keys.push(key);
                    }
                }
            }
            thisObj = thisObj || obj;
            return keys.filter(function(key){
                    return callback.call(thisObj, key, $NS.type.typeOf(obj[key]), Object.getOwnPropertyDescriptor(obj, key), obj );
                });
        },


        /**
         * Creates a new object by filtering properties from a source object. Method works similar 
         * {@link fs.obj.filterNames}, but it produces an a real object as a result instead of an 
         * array of property names. To filter properties the function runs a callback for each object
         * property. Result object contains attributes for which the callback has returned true.   
         * 
         * @name fs.obj.filter
         * @info Creates a new object by filtering properties from a source object.
         * @see $NS.obj.filterPropertyNames
         * @function
         * @requires fs.obj.filterPropertyNames
         * 
         * @param {Object} obj Object to filter
         * @param {function} callback A callback function - will be executed for each object's property
         * @param {Object} [thisObj=obj] A value of this property inside a callback
         * @param {boolean} [includeNotEnumerable=false] - flag which says that callback should be run also for non-enumerable properties
         * @returns {Object} a sub-object
         * 
         * @example
         * var person = {
         *       firstname: "John",
         *       lastname: "Ubot",
         *       getFullName: function(){ return this.firstname+" "+this.lastname },
         *       age: 42,
         *       sex: "M"
         *    };  
         *    
         * // selects from person only those properties which point to
         * // non-functions and which contain string "name"
         * // result: { firstname: "John", lastname: "Ubot" } 
         * var newObj = fs.obj.filter(person, function(name, type, desc){
         *       return type !== "function" && /name/.test(name);
         *    });
         */
        filter: function(obj, callback, thisObj, includeNotEnumerable) {
            var names = this.filterPropertyNames(obj, callback, thisObj, includeNotEnumerable);
            var result = {};
            for( var i=0, len=names.length; i < len; ++i ) {
                result[names[i]] = obj[names[i]];
            }
            return result;
        },


        /**
         * Copies only those properties from the source which does not exist in destination object
         * @name fs.obj.safeMixin
         * @info Mixin without override ability
         * @see $NS.mixin
         * @function
         * @returns {Object}
         */
        safeMixin: function(dst){
            var src = null, prop = null;
            for(var srcId=1, len=arguments.length; srcId < len; ++srcId) {
                for(prop in (src=arguments[srcId])) { 
                    if( src.hasOwnProperty(prop) && !dst.hasOwnProperty(prop) ){
                        dst[prop] = src[prop];
                    }
                }
            }
            return dst;            
        },


        /**
         * Checks if an object contains given property. By default it works like a standard
         * Object.prototype.hasOwnProperty method, but it also provides some enhancements:<ul><li>
         * 1. It works properly on DOM elements under Internet Explorer (where hasOwnProperty
         *    method is not supported)</li><li>
         * 2. It allows to examine also object's prototype chain (depends on a value of 
         *    the third attribute)</li><li>
         * 3. It allows to include or exclude non-enumerable properties (depends on the 
         *    value of the forth parameter).</li></ul>
         * 
         * @name fs.obj.contains
         * @info Checks if an object contains given property.
         * @requires fs.obj.isEnumerable
         * @function
         * 
         * @param {Object} obj
         * @param {string} propertyName
         * @param {boolean} [considerPrototypes=false]
         * @param {boolean} [enumerableOnly=false]
         * @returns {boolean}
         * 
         * @example
         * // an equivalent of hasOwnProperty 
         * $NS.obj.contains(Array.prototype, "slice") === true;
         * // false, because "slice" is not enumerable 
         * $NS.obj.contains(Array.prototype, "slice", false, false) === false;
         * // false, "slice" is not direct property of Array instance
         * $NS.obj.contains([], "slice") === false;
         * // true, because test of prototype chain has been requested
         * $NS.obj.contains([], "slice", true) === true; 
         */
        contains: function(obj, propertyName, considerPrototypes, enumerableOnly){
            __throwTypeErrorForNonObject( obj );
            var exists = !!considerPrototypes ? propertyName in obj : Object.prototype.hasOwnProperty.call(obj, propertyName);
            return exists && (!enumerableOnly || this.isEnumerable(obj, propertyName, considerPrototypes));
        },


        /**
         * Returns the number of own enumerable properties inside an object.
         * 
         * @name fs.obj.getLength
         * @info Returns the number of own enumerable properties inside the object.
         * @function
         * 
         * @param {Object} obj Object to test.
         * @returns {number} A number of own enumerable properties inside an object
         */
        getLength: function(obj){
            __throwTypeErrorForNonObject( obj );
            
            var len = 0;
            for(var key in obj) {
                if( Object.prototype.hasOwnProperty.call(obj, key) ) {
                    ++len;
                } 
            }
            return len;
        },


        /**
         * Returns a reference to the global object (even in the strict mode). 
         * 
         * @name fs.obj.getGlobal
         * @info Return a reference to the global object (works in the strict mode too)
         * @function
         * 
         * @param {boolean} [dontThrow=false] When set to true will not throw any error if global object not found.
         * @returns {Object} The global object
         * @throws {TypeError} when impossible to determine the global object. It may
         *         happen when SES is used
         */
        getGlobal: function(dontThrow) {
            var glob = (function(){ return this; })();
            if( glob ) {
                return glob;
            }
            
            if( typeof window === 'object' ) {
                return window;
            } 
            
            if( !!(glob = new Function("return this;")()) ) {
                return glob;
            }
            
            var e = eval;
            if( !!(glob = e("this")) ) {
                return glob;
            }
            
            glob = typeof global === 'object' ? global : ( typeof GLOBAL === 'object' ? GLOBAL : null );
            if( glob && glob.Array && glob.isNaN && glob.TypeError ) {
                return glob;
            }
            
            if( !dontThrow ) {
                throw new TypeError("Impossible to determine the global object");
            }
        },


        /**
         * Object is considered to be empty when the number of own, enumerable properties
         * is equal to zero. The function does not check the value of length internal property.
         * 
         * @name fs.obj.isEmpty
         * @info Function checks if the object is empty.
         * @requires fs.obj.getLength
         * @function
         * 
         * @param {*} obj An object to examine
         * @returns {boolean} true if object is empty, false otherwise
         * 
         * @example $NS.obj.isEmpty([]) === true;
         * @example 
         * var arr = [];
         * arr.addNewElement(elem) { this.push(elem); };
         * $NS.obj.isEmpty(arr) === false;
         */
        isEmpty: function(obj) {
            return this.getLength(obj) === 0;
        },


        /**
         * Checks whether the property of given object is enumerable. It works like standard
         * Object.prototype.propertyIsEnumerable method with following enhancements:
         * 1. It works properly for DOM elements in Internet Explorer where propertyIsEnumerable
         *    method is not available
         * 2. It allows to check either direct object properties or also properties in its prototype chain.
         * 
         * @name fs.obj.isEnumerable
         * @info Cross-environment version of standard propertyIsEnumerable method
         * @function
         *  
         * @param {Object} obj an object
         * @param {string} propertyName name of object's property
         * @param {boolean} [considerPrototypes=false] If set to true object's prototype chain will be tested as well
         * @returns {boolean} true if property is enumerable, false otherwise
         */
        isEnumerable: function(obj, propertyName, considerPrototypes){
            __throwTypeErrorForNonObject( obj );
            
            if( !considerPrototypes && typeof obj.propertyIsEnumerable === "function" ) {
                return obj.propertyIsEnumerable( propertyName );
            }
            if( ! (!!considerPrototypes ? propertyName in obj : Object.prototype.hasOwnProperty.call(obj, propertyName)) ) {
                return false;
            }
            for(var key in obj) {
                if( key === propertyName ){
                    return true;
                }
            }
            return false;            
        },


        /**
         * Implementation of ToPropertyDescriptor inner ECMAScript 5 method.
         * Function returns object representing property descriptor based on 
         * input parameter.
         * ECMAScript 5 reference: 8.10.5
         * 
         * @name fs.obj.toPropertyDescriptor
         * @info Creates property descriptor from a given object.
         * @function 
         * 
         * @param {Object} obj a property object
         * @returns {Object} a property descriptor
         * @throws {TypeError} when obj is not an object or when accessor and 
         *             writable/value properties provided at the same time
         * 
         */
        toPropertyDescriptor: function(obj){
            __throwTypeErrorForNonObject( obj );
            
            var desc = {};
            obj.hasOwnProperty("enumerable") && ( desc.enumerable = !!obj.enumerable );
            obj.hasOwnProperty("configurable") && ( desc.configurable = !!obj.configurable );
            obj.hasOwnProperty("writable") && ( desc.writable = !!obj.writable );
            obj.hasOwnProperty("value") && ( desc.value = obj.value );
            
            if( obj.hasOwnProperty("get") ) {
                if( !__utils.isFunction(obj.get) && typeof obj.get !== 'undefined' ) {
                    throw new TypeError( "Getter must be a callable object" );
                }
                desc.get = obj.get;
            }
            
            if( obj.hasOwnProperty("set") ) {
                if( !__utils.isFunction(obj.set) && typeof obj.set !== 'undefined' ) {
                    throw new TypeError( "Setter must be a callable object" );
                }
                desc.set = obj.set;
            }        
            
            if( (desc.hasOwnProperty("get") || desc.hasOwnProperty("set")) 
                    && (desc.hasOwnProperty("writable") || desc.hasOwnProperty("value")) ) {
                throw new TypeError("Invalid property. A property cannot both have accessors and be writable or have a value");
            }

            return desc;        
        }




    };
    
})();


/**
 * @name fs.str
 * @namespace Module provides set of functions for string manipulation
 * @static
 */
$NS.str = (function(){


    
    return {
        
        /**
         * @name fs.str.escapeHTML
         * @info Escapes HTML special characters in a string
         * @function
         * 
         * @param {string} str
         * @returns {string}
         */
        escapeHTML: (function(){
            
            var __MAP = {
                    '&': '&amp;',
                    '<': '&lt;',
                    '>': '&gt;',
                    '\\': '\\\\',
                    '"': '\"'
                },
            
                __replaceFn = function(c) {
                    return __MAP[c];
                },
                
                __pattern = /[<>&]/g
            ;
            
            return function(str) {
                return String(str).replace( __pattern, __replaceFn );
            };
            
        })(),


        /**
         * @name fs.str.unescapeHTML
         * @info Unescapes HTML special characters from a string
         * @function
         * 
         * @param {string} str
         * @returns {string}
         */
        unescapeHTML: (function(){
            
            var __MAP = {
                    '&lt;': '<',
                    '&gt;': '>',
                    '&amp;': '&',
                    '\"': '"',
                    '\\\\': '\\'
                },
            
                __replaceFn = function(c) {
                    return __MAP[c];
                },
                
                __pattern = /&gt;|&lt;|&amp;/g
            ;
            
            return function(str) {
                return String(str).replace( __pattern, __replaceFn );
            };
            
        })(),


        /**
         * @name fs.str.unescapeURI
         * @info Returns decoded URI string (fixes problems with standard decodeURIComponent)
         * @function
         * 
         * @param {string} uri URI string
         * @returns {string}
         */
        unescapeURI: function(uri){
            return decodeURIComponent( uri.replace(/\+/g, " ") );
        },


        /**
         * @name fs.str.escapeURI
         * @info Delegation of standard encodeURIComponent (for coherent convention with unescapeURI)
         * @function
         * 
         * @param {string} str a string
         * @returns {string} Escaped URI string 
         */
        escapeURI: function(str) {
            return encodeURIComponent( str );
        },


        /**
         * @name fs.str.trim
         * @info Trims the string from left and right side.
         * @function
         * 
         * @param {string} input string; in non-string passed it will be converted to string
         * @returns {string} trimmed string
         */
        trim: (function(){
            var __LTRIM = /^\s\s*/,
                __RTRIM = /\s\s*$/;
            
            return function(str){
                return String(str).replace(__LTRIM, '').replace(__RTRIM, '');
            };
        })(),


        /**
         * Produces formated string for {param}-style templates.
         * 
         * @name fs.str.format
         * @requires fs.obj.access
         * @info Produces formated string for {param}-style templates
         * @function
         * 
         * @param {string} str Template string
         * @param {Object|string} data Object with parameters or list of values (multiple attributes of the function) 
         * @returns {string} Formatted text.
         * 
         * @example $NS.str.format("Hello, I'm {0}, age {1}", "David", 34);
         * @example $NS.str.format("Hello, I'm {0}, age {1}", ["David", 34]);
         * @example $NS.str.format("Hello, I'm {name}, age {age}", {name: "David", age:34});
         */
        format: function(str, data) {
            var args = String(str).replace(/\{\{|\}\}/g,'').match(/\{[^{}]+\}/g) || [],
                params = arguments.length > 2 ? Array.prototype.slice.call(arguments, 1) : (typeof data === 'object' ? data : [data]);
            
            for( var arg; !!(arg=args.shift()); ) {
                var name = arg.substr(1, arg.length-2);
                str = str.replace( arg, $NS.obj.access(params, name) );
            }
            
            return str ? str.replace(/\{\{/g,"{").replace(/\}\}/g,"}") : str;
        },


        /**
         * Repeats string n-times.
         * 
         * @name fs.str.repeat
         * @info Repeats string n-times.
         * @function
         * 
         * @param {string} str
         * @param {number} [times=1]
         * @returns {string}
         */
        repeat: function(str,times) {
            var res = str;
            times = isNaN(times) ? 0 : times;
            while( --times > 0 ) {
                res += str;
            }
            return res;
        },


        /**
         * Returns reversed string.
         * 
         * @name fs.str.reverse
         * @info Returns reversed string.
         * @function
         * 
         * @param {string} str
         * @returns {string} Reversed string.
         */
        reverse: function(str) {
            return String(str).split('').reverse().join('');
        },


        /**
         * Looks if given text contains a phrase and returns a boolean.
         * 
         * @name fs.str.contains
         * @info Checks whether string contains given phrase.
         * @nosideeffects
         * @function
         * 
         * @param {string} str A string for test.
         * @param {string} searchStr A phrase to find in the string. 
         * @returns {boolean} True if searchStr found in the str, false otherwise.
         */
        contains: function(str, searchStr) {
            return String(str).indexOf(searchStr) >= 0;
        },


        /**
         * Makes a string camel-case
         * @name fs.str.camelize
         * @info Makes a string camel case
         * @function
         * @param {string} str A string
         * @returns {string} camelized string
         * @example $NS.str.camelize('this-is_a_test') === 'thisIsATest';
         */
        camelize: (function(){
            var __pattern = /[_\-][a-z]/g,
                __camelizer = function(x){ return x.charAt(1).toUpperCase(); };
            return function(str) {
                return String(str).replace( __pattern, __camelizer ); 
            };
        })()




    };
    
})();


// fs.dom module
$NS.dom = (function(){

    // exit if DOM is not supported
    // note: JS-DOM and other DOM emulators should be loaded before FunctionSack
    if( typeof document === 'undefined' || typeof document.createElement === 'undefined' ) {
        return {};
    }

    var __D = document,
    
        __slice = Array.prototype.slice,
        
        __elem = function(el) {
            return typeof el === 'string' ? $NS.dom.byId(el) : el;
        }
    ;


    
    return {
        
        /**
         * Returns an element (or elements) of a given ID (or IDs).
         * When only one attribute passed function return an element. 
         * For more than one attribute it will return an array of elements. 
         * @name fs.dom.byId
         * @info Returns DOM element(s) of given ID(s)
         * @function
         * @param {string} is element's id
         * @returns {Element|Array} HTML element or null
         * @example $NS.dom.byId('myElem')
         */
        byId: function(id) {
            if( arguments.length < 2 ) {
                return __D.getElementById(id);
            }
            var arr = [];
            for(var i=0, len=arguments.length; i < len; ++i) {
                arr.push( __D.getElementById(arguments[i]) );
            }
            return arr;
        },


        /**
         * Returns list of element selected by tag name
         * @name fs.dom.byTagName
         * @info Returns list of elements of given tag name
         * @function
         * @param {Object} tagName
         * @returns {NodeList} list of nodes
         * @example $NS.dom.byTagName('head')
         */
        byTagName: function(tagName){
            return __D.getElementsByTagName(tagName);
        },


        /**
         * This function is mostly useful for libraries and util functions
         * than for direct use. Thanks to it there possible to avoid recognition
         * if someone passed to our code a DOM element or its ID - exactly the 
         * same way like in most of functions in this module.
         * 
         * @name fs.dom.getElement
         * @info Accepts string or HTML element as parameter; when string passed works like dom.byId
         * @function
         * @param {string|Element} el DOM element or its ID
         * @returns {Element} DOM element
         * 
         * @example 
         *   function validateFormElement(elem) {
         *      elem = fs.dom.getElement(elem);
         *      // your code here
         *   }
         *   // usage (both examples work the same way):
         *   validateFormElement("firstname");
         *   validateFormElement(fs.dom.byId("firstname"));
         */
        getElement: __elem,


        /**
         * Creates a DOM element object base on input parameters.
         * 
         * @name fs.dom.create
         * @info creates new DOM element
         * @function
         * @param {string} tag A name of the element
         * @param {Object} [params] The element attributes; optional
         * @param {string|Node} [body] A body of the element; optional
         * @returns {Element} created element
         * 
         * @example $NS.dom.create('div')
         * @example $NS.dom.create('div', {id: 'main', 'class': 'big'}, 'Some text here')
         * @example $NS.dom.create('div', {}, $NS.dom.create('img', {src: 'myimg.png'}) )
         */
        create: function(tag, params, body) {
            var node = __D.createElement( tag );
            for(var param in params) { if(params.hasOwnProperty(param)) {
                node.setAttribute( param, params[param] );
            }}
            if( arguments.length > 2 ){
                var bodyElements = Array.prototype.slice.call(arguments, 2);
                while( !!(body=bodyElements.shift()) ){
                    if( typeof body === 'object' && body.appendChild ) {
                        node.appendChild( body );
                    } else {
                        node.appendChild( __D.createTextNode(body) );
                    }
                }
            }
            return node;
        },


        /**
         * Appends given node to parent. Can be easy combined with
         * $NS.dom.create function.
         * @name fs.dom.append 
         * @info Appends DOM element(s) to a given element 
         * @function
         * @param {string|Node} parent node or its id
         * @param {Node} an element to be added
         * @example $NS.dom.append('menu', $NS.dom.create('div'))
         */
        append: function(elem /*, childNodes ... */) {
            var parentNode = __elem( elem ),
                childNodes = __slice.call( arguments, 1 ),
                node;
            
            while( !!(node = childNodes.shift()) ) {
                parentNode.appendChild( node );
            }
        },


        /**
         * Removes DOM element
         * @name fs.dom.remove
         * @info Removes DOM element
         * @function
         * @example $NS.dom.remove('myplace')
         * @example $NS.dom.remove($NS.dom.byId('myplace'))
         * @param {string | Element} elem A DOM element or its id
         */
        remove: function(elem) {
            var node = __elem( elem );
            node && node.parentNode && node.parentNode.removeChild(node);
        },


        /**
         * Removes all children from given DOM node.
         * @name fs.dom.removeChildNodes
         * @info Removes all children of given DOM element
         * @function
         * @param {string | Element} elem A DOM element or its id
         * @example $NS.dom.removeChildNodes('myplace')
         * @example $NS.dom.removeChildNodes($NS.dom.byId('myplace'))
         */
        removeChildNodes: function(elem){
            var node = __elem( elem );
            while(node.hasChildNodes()){
                node.removeChild(node.firstChild);
            }
        },


        /**
         * Safely removes DOM element - to avoid memory leaks on older browsers.
         * The method recursively removes all children of given node and at the
         * end removes the node itself. 
         * @name fs.dom.destroy
         * @info Safely (recursively) removes DOM element
         * @function
         * @param {string | Element} elem  A DOM element or its id
         * @example $NS.dom.destroy('menu')
         * @example $NS.dom.destroy($NS.dom.byId('menu'))
         */
        destroy: function(elem) {
            var node = __elem( elem );
            while( node && node.hasChildNodes() ) {
                this.destroy( node.firstChild );
            }
            this.remove(node);
        },


        /**
         * Checks if the given element has a CSS class
         * @name fs.dom.hasClass
         * @info Checks if the given element has a CSS class
         * @function
         * @param {string | Element} elem A DOM element or its id
         * @param {string} className A CSS class name
         * @returns {boolean} 
         */
        hasClass: function(elem, className) {
            var node = __elem( elem );
            var pattern = new RegExp("(\\s|^)("+className+")(\\s|$)");
            return pattern.test( node.className );
        },


        /**
         * Adds new CSS class (or classes) to given element. The class
         * won't be added if already exist. 
         * @name fs.dom.addClass
         * @info Adds new CSS class(es) to a given element.
         * @requires fs.dom.hasClass
         * @function
         * @param {string | Element} elem A DOM element or its id
         * @param {string} class1 A CSS class name
         * @returns {number} number of classes added to the element
         */
        addClass: function(elem, class1 /*, ...*/) {
            var node = __elem( elem ),
                args = __slice.call( arguments, 1 ),
                newClasses = [];
            
            for(var i=0, len=args.length; i < len; ++i) {
                this.hasClass(node, args[i]) || newClasses.push(args[i]);
            }
            
            if( newClasses.length ) {
                node.className += (node.className.length ? " " : "") + newClasses.join(" ");
            }
            
            return newClasses.length;
        },


        /**
         * Removes CSS class (or classes) from a given element
         * @name fs.dom.removeClass
         * @info Removes CSS class from given element.
         * @function
         * @param {string | Element} elem DOM element or its id
         * @param {string} class1 A CSS class name
         * @returns {number} number of removed CSS classes
         */    
        removeClass: function(elem, class1 /*,...*/) {
            var node = __elem( elem ),
                args = __slice.call( arguments, 1 ),
                classes = node.className.split(/\s+/),
                alen = args.length,
                result = 0;
            
            while( alen-- ) {
                for( var i=classes.length-1; i >= 0; --i ) {
                    if( args[alen] === classes[i] ) {
                        classes.splice( i, 1 );
                        ++result;
                    }
                }
            }
            
            node.className = classes.join(" ");
            return result;
        },


        /**
         * Replaces CSS class with another one
         * @name fs.dom.replaceClass
         * @info Replaces CSS class with another one
         * @function
         */
        replaceClass: function(elem, srcClass, dstClass) {
            var e = __elem( elem );
            this.hasClass( e, srcClass ) && this.removeClass( e, srcClass );
            this.addClass( e, dstClass );
        },


        /**
         * Hides DOM element(s)
         * @name fs.dom.hide
         * @info Hides DOM element
         * @function
         * @param {string | Element} elem A DOM element or its id
         */
        hide: function(/* ... */) {
            var args = __slice.call( arguments ),
                node;
            
            while( !!(node = args.shift()) ) {
                node = __elem( node );
                if( node.style.display !== 'none' ) {
                    node.__lastDisplay = node.style.display;
                    node.style.display = "none";
                }
            }
        },


        /**
         * Shows the hidden element(s)
         * @name fs.dom.show
         * @info Shows hidden DOM element
         * @function
         * @param elem {string | element} DOM element or its id
         */
        show: function(elem) {
            var args = __slice.call( arguments ),
                node;
            
            while( !!(node = args.shift()) ) {
                node = __elem(node);
                node.style.display = node.__lastDisplay || "";
            }
        },


        /**
         * Sets text for the node
         * TODO: basic xss prevention removing script tags
         * @name fs.dom.setText
         * @info Set text to a DOM element.
         * @function
         * @param {Object} elem
         * @param {String} text
         * @param {String} [defaultValue]
         */
        setText: function(elem, text, defaultValue){
            var node = __elem( elem );
            var f;
            if( "textContent" in node ) {
                f = function(elem, text, defaultValue){
                    var node = __elem( elem );
                    node.textContent = text;
                };
            } else if( "innerText" in node ) {
                f = function(elem, text, defaultValue){
                    var node = __elem( elem );
                    node.innerText = text;
                };
            } else {
                f = function(elem, text, defaultValue){
                    var node = __elem( elem );
                    while(node.hasChildNodes()){
                        node.removeChild(node.firstChild);
                    }
                    node.appendChild(document.createTextNode(text));
                };
            }
            this.setText = f;
            return f.apply(this, arguments);
        },


        /**
         * Function sets HTML for the element
         * @name fs.dom.setHTML
         * @info Sets HTML to an element
         * @function
         * @param {Object} elem
         * @param {String} html
         * @param {String} [defaultValue]
         */
        setHTML: function(elem,html,defaultValue){
            var node = __elem( elem );
            if(node){
                node.innerHTML = html || defaultValue || "";
            }
        },


        /**
         * Get/set CSS style attribute
         * @name fs.dom.style
         * @info get/set CSS style attribute
         * @requires fs.str.camelize
         * @requires fs.toArray
         * @function
         * 
         * @param {Element|string|Array} elem DOM element or its ID (or an array of elements or IDs)
         * @param {string|Object} propertyName A name of style property
         * @param {string} [value] A value of style property
         * @returns {string|Array}
         * 
         * @example var bgcolor = $NS.dom.style("menu","background-color");
         * @example $NS.dom.style(myMenu, "display", "none");
         */
        style: function(elems, propertyName, value){
            elems = $NS.toArray(elems);
            var results = new Array( elems.length ),
                isObj = __utils.isObject(arguments[1]),
                i = null;
            
            if( isObj ) {
                var obj = {};
                for( i in propertyName ) { if(propertyName.hasOwnProperty(i)) {
                    obj[ $NS.str.camelize(i) ] = propertyName[i];
                }}
                propertyName = obj;
            } else {
                propertyName = $NS.str.camelize( propertyName );
            }
            
            for( var i=0, len=elems.length; i < len; ++i ) {
                var elem = __elem( elems[i] );
                if( isObj) {
                    __utils.mixin(elem.style, arguments[1]);
                    results[i] = arguments[1];
                } else {
                    results[i] = ( arguments.length < 3
                            ? elem.style[propertyName]
                            : (elem.style[propertyName] = value) );
                }
            }
            return results.length === 1 ? results[0] : results;
        },


        /**
         * Adds given function as a listener to a DOM element
         * @name fs.dom.addListener
         * @requires fs.toArray 
         * @info Adds given function as a listener to a DOM element
         * @function
         * @see $NS.dom.listenOnce
         * 
         * @param {Element|string|Array} elem DOM Element or its ID. 
         * @param {string} eventName Name of an event without "on" prefix (eg. "click") 
         * @param {function} handler Event listener function
         * @param {boolean} [capturing=false] enables capturing phase
         * @returns {function} Event listener; in most of the cases result is equal to 'handler'
         *          attribute, although on older IE versions (where only attachEvent is available),
         *          the handler must be wrapped with another function. addListener returns the real
         *          event listener in this case.
         *          
         * @example $NS.dom.addListener('menu', 'click', function(event){ ... });
         * @example $NS.dom.addListener(document.getElementsByTagName('input'),'blur',validator);
         */
        addListener: (function() {
            if( window.addEventListener ) {
                return function(elems, eventName, handler, capturing) {
                    elems = $NS.toArray(elems);
                    for(var i=0, len=elems.length; i < len; ++i){
                        var elem = __elem(elems[i]);
                        elem.addEventListener(eventName, handler, !!capturing);
                    }
                    return handler;
                };
            }
                
            // TODO stop propagation and capturing
            if( window.attachEvent ) {
                return function(elems, eventName, handler) {
                    elems = $NS.toArray(elems);
                    var ieHandler = function(){
                        handler.call( window.event.srcElement, window.event );
                    };
                    for(var i=0, len=elems.length; i < len; ++i){
                        elem = __elem(elems[i]);
                        elem.attachEvent( "on"+eventName, ieHandler );
                    }
                    return ieHandler;
                };
            }
                
            return function(elems, eventName, handler) {
                elems = $NS.toArray(elems);
                for(var i=0, len=elems.length; i < len; ++i){
                    var elem = __elem(elems[i]);
                    elem[ "on"+eventName ] = handler;
                }
                return handler;
            };
        })(),


        /**
         * Adds listener for single time execution. The event listener will be 
         * automatically removed after first execution.
         * @name fs.dom.listenOnce
         * @info Adds listener for one time execution.
         * @requires fs.dom.addListener
         * @requires fs.toArray
         * @requires fs.bind
         * @function
         * @see $NS.dom.addListener
         * 
         * @param {Element|string|Array} elem DOM Element or its ID or array of them. 
         * @param {string} eventName Name of an event without "on" prefix (eg. "click") 
         * @param {function} handler Event listener function
         * @param {boolean} [capturing=false] enables capturing phase
         * @returns {function} Event listener
         */
        listenOnce: function(elems, eventName, handler, capturing){
            elems = $NS.toArray(elems);
            function fh(event){
                    handler.call(this, event);
                    $NS.dom.removeListener(this, eventName, fh, capturing);
                };
            for(var i=0, len=elems.length; i < len; ++i){
                var elem = __elem(elems[i]);
                this.addListener(elem, eventName, fh.bind(elem), capturing);
            }
            return fh;
        },


        /**
         * Removes event listener from given DOM element 
         * @name fs.dom.removeListener
         * @info Removes event listener from given DOM element 
         * @function
         */
        removeListener: (function(){
            if( window.removeEventListener ) {
                return function(elem, eventName, handler, capturing) {
                    elem = __elem(elem);
                    elem.removeEventListener(eventName, handler, !!capturing);
                };
            }
            
            if( window.detachEvent ) {
                return function(elem, eventName, handler) {
                    elem = __elem(elem);
                    elem.detachEvent( "on"+eventName, handler );
                };
            }
            
            return function(elem, eventName){
                elem = __elem(elem);
                elem[ "on"+eventName ] = null;
            };
        })(),


        /**
         * Triggers an event
         * @name fs.dom.dispatchEvent
         * @info Triggers an event
         * @function
         */
        dispatchEvent: (function(){
            if(document.createEvent) {
                return function(elem, eventName, eventData){
                    var myEvent = document.createEvent( "Event" );
                    elem = __elem(elem);
                    myEvent.initEvent( eventName, true, true );
                    eventData && __utils.mixin( myEvent, eventData );
                    elem.dispatchEvent( myEvent );
                };
            } else if(document.createEventObject) {
                return function(elem, eventName, eventData){
                    var myEvent = document.createEventObject(window.event);
                    elem = __elem(elem);
                    eventData && __utils.mixin( myEvent, eventData );
                    elem.fireEvent("on"+eventName, myEvent);
                };
            } else {
                return function(){
                    throw new Error("Event dispatching is not supported");
                };
            }
        })()




    };
    
})();


// fs.bom module
$NS.bom = (function(){


    
    return {
        
        /**
         * Returns object representation of query string
         * @name fs.bom.parseQueryString
         * @info returns object representation of query string
         * @requires fs.str.unescapeURI
         * @function
         * @param {string} [queryString] A query string; if not specified window.location.search will be taken
         * @returns {Object} A map of query parameters
         */
        parseQueryString: function(queryString) {
            var params = {},
                query = (queryString || window.location.search || "").replace(/^\?/,""),
                pattern = /([^&=]+)=?([^&]*)/g,
                tmp = null;
            while( !!(tmp = pattern.exec(query)) ) {
                    params[ $NS.str.unescapeURI(tmp[1]) ] = $NS.str.unescapeURI( tmp[2] );
            }
            return params;
        }




    };
    
})();


// fs.ajax module
$NS.ajax = (function(){

    var __getAjaxObject = function(){
        if( typeof XMLHttpRequest !== 'undefined' ) {
            return new XMLHttpRequest();
        } else if(typeof ActiveXObject !== 'undefined') {
            return new ActiveXObject('Microsoft.XMLHTTP');
        }
        throw new Error("AJAX not supported");
    };
    

    
    return {
        
        /**
         * Asynchronously loads data as a plain text. 
         * Configuration object: url, onError, params, method
         * @name fs.ajax.loadSync
         * @info Asynchronously loads data as a plain text. 
         * @function
         * @param {Object} conf A configuration object
         * @returns {string} Server output
         */
        loadSync: function(conf) {
            var request = __getAjaxObject();  
            request.open( 'GET', conf.url, false );
            if( request.overrideMimeType ) {
                request.overrideMimeType("text/plain; charset=utf-8");
            }
            // request.setRequestHeader("Content-Type","application/x-www-form-urlencoded"); // add for post
            request.send(null);  
            if( request.status === 0 || request.status === 200 ) {
                return request.responseText; 
            } else { 
                conf.onError && conf.onError( request.status );
            }
        }




    };
    
})();


// fs.es5 module
(function(){

    
    /**
     * Checks features of the JavaScript engine 
     * @private
     */
    var __features = {
            STRING_INDEX_ENUMERABLE: "abc".propertyIsEnumerable("1"),
            ACCESSORS: Object.prototype.__defineGetter__ && Object.prototype.__defineSetter__,
            DOM: typeof window === 'object' && typeof document === 'object'
        };
        
        
    var __isCallable = function(obj){
        return typeof obj === "function";
    };
    
        
    var $AP = Array.prototype;
    
    
    /**
     * Safe equivalent of Object.prototype.propertyIsEnumerable. The original method 
     * is not available on older versions of IE for global object (same as hasOwnProperty)
     * @private 
     */
    var __propertyIsEnumerable = function(obj, property) {
        if( obj.propertyIsEnumerable ) {
            return obj.propertyIsEnumerable(property);
        }
        for(var key in obj) {
            if( key === property && (obj.hasOwnProperty ? obj.hasOwnProperty(property) : true) ){
                return true;
            }
        }
        return false;
    };

    /**
     * Returns first valid index of an array
     * @name __firstIndex
     * @private
     */
    var __firstIndex = function(arr) {
        for( var k=0, len=arr.length; k < len; ++k ) {
            if( arr.hasOwnProperty(String(k)) ) {
                return k;
            }
        }    
        return -1;
    };


    /**
     * Internal method of this library which checks additional conditions in property descriptor object
     * in order to limitations in ECMAScript 3. Should be called on a result of __toPropertyDescriptor 
     * internal function.
     * @name __applyDefaults
     * @private
     * @param {Object} desc a property descriptor
     * @param {boolean} defaultValue the default value of not provided flags
     * @param value the value of a property
     * @returns {Object} property descriptor with applied defaults and value 
     * @see __toPropertyDescriptor
     */
    var __applyDefaults = function(desc, defaultValue, value) {
        if(desc.hasOwnProperty("get") || desc.hasOwnProperty("set")) {
            throw new TypeError( "Getters and setters are not supported by this ECMAScript engine" );
        } else {
            desc.writable = desc.hasOwnProperty('writable') ? desc.writable : defaultValue;
            desc.value = desc.hasOwnProperty('value') ? desc.value : value;
        }
        
        desc.enumerable = desc.hasOwnProperty('enumerable') ? desc.enumerable : defaultValue;
        desc.configurable = desc.hasOwnProperty('configurable') ? desc.configurable : defaultValue;
        
        var t = null;
        if( (!desc[t="configurable"]) || (!desc[t="enumerable"]) || (!desc[t="writable"]) ) {
            throw new TypeError( "Property '".concat(t,"' cannot be set to false in this version of ECMAScript engine") );
        }        

        return desc;                
    };


    /**
     * Implementation of ToPropertyDescriptor inner ECMAScript 5 method.
     * ECMAScript 5 reference: 8.10.5
     * @name __toPropertyDescriptor
     * @private
     * @param {Object} obj a property object
     * @returns {Object} a property descriptor
     */
    var __toPropertyDescriptor = function(obj){
        if( !obj || typeof obj !== 'object' ) {
            throw new TypeError( obj+" is not an object" );
        }
        
        var desc = {};
        obj.hasOwnProperty("enumerable") && ( desc.enumerable = !!obj.enumerable );
        obj.hasOwnProperty("configurable") && ( desc.configurable = !!obj.configurable );
        obj.hasOwnProperty("writable") && ( desc.writable = !!obj.writable );
        obj.hasOwnProperty("value") && ( desc.value = obj.value );
        
        if( obj.hasOwnProperty("get") ) {
            if( !__isCallable(obj.get) && typeof obj.get !== 'undefined' )
                throw new TypeError( "Getter must be a callable object" );
            desc.get = obj.get;
        }
        
        if( obj.hasOwnProperty("set") ) {
            if( !__isCallable(obj.set) && typeof obj.set !== 'undefined' ){
                throw new TypeError( "Setter must be a callable object" );
            }
            desc.set = obj.set;
        }        
        
        if( (desc.hasOwnProperty("get") || desc.hasOwnProperty("set")) 
                && (desc.hasOwnProperty("writable") || desc.hasOwnProperty("value")) ) {
            throw new TypeError("Invalid property. A property cannot both have accessors and be writable or have a value");
        }

        return desc;        
    };


    /**
     * Attributes which are marked as non-enumerable by the internal ECMAScript flag.
     * Because in ECMAScript 3 there is not possible to set enumerable flag from the
     * language level - they should be the only non-enumerable elements in the language.
     * (Maybe apart some DOM elements which should be added to this implementation later)
     * 
     * @name __notEnumerableProperties
     * @requires fs.obj.getGlobal
     * @private
     * @type Array
     */
    var __notEnumerableProperties = (function(){
        
        var props = [
             {
                 object: Object,
                 keys: ['getOwnPropertyNames', 'seal', 'create', 'isFrozen', 'keys', 'isExtensible', 
                        'getOwnPropertyDescriptor', 'preventExtensions', 'getPrototypeOf', 'defineProperty', 'isSealed', 
                        'defineProperties', 'freeze']
             },{
                 object: Object.prototype,
                 keys: ['toString', '__lookupGetter__', '__defineGetter__', 'toLocaleString', 'hasOwnProperty', 'valueOf', '__defineSetter__', 
                        'propertyIsEnumerable', 'isPrototypeOf', '__lookupSetter__']
             },{
                 object: Function.prototype,
                 keys: ['bind', 'arguments', 'toString', 'length', 'call', 'name', 'apply', 'caller']
             },{
                 object: Number,
                 keys: ['NaN', 'NEGATIVE_INFINITY', 'POSITIVE_INFINITY', 'MAX_VALUE', 'MIN_VALUE']
             },{
                 object: Number.prototype,
                 keys: ['toExponential', 'toString', 'toLocaleString', 'toPrecision', 'valueOf', 'toFixed']
             },{
                 object: String,
                 keys: ['fromCharCode']
             },{
                 object: String.prototype,
                 keys: ['length', 'concat', 'localeCompare', 'substring', 'italics', 'charCodeAt', 'strike', 'indexOf', 
                        'toLowerCase', 'trimRight', 'toString', 'toLocaleLowerCase', 'replace', 'toUpperCase', 'fontsize', 'trim', 'split', 
                        'substr', 'sub', 'charAt', 'blink', 'lastIndexOf', 'sup', 'fontcolor', 'valueOf', 'link', 'bold', 'anchor', 'trimLeft', 
                        'small', 'search', 'fixed', 'big', 'match', 'toLocaleUpperCase', 'slice']
             },{
                 object: Boolean.prototype,
                 keys: ['toString', 'valueOf']
             },{
                 object: Date,
                 keys: ['now', 'UTC', 'parse']
             },{
                 object: Date.prototype,
                 keys: ['toUTCString', 'setMinutes', 'setUTCMonth', 'getMilliseconds', 'getTime', 'getMinutes', 'getUTCHours', 
                        'toString', 'setUTCFullYear', 'setMonth', 'getUTCMinutes', 'getUTCDate', 'setSeconds', 'toLocaleDateString', 'getMonth', 
                        'toTimeString', 'toLocaleTimeString', 'setUTCMilliseconds', 'setYear', 'getUTCFullYear', 'getFullYear', 'getTimezoneOffset', 
                        'setDate', 'getUTCMonth', 'getHours', 'toLocaleString', 'toISOString', 'toDateString', 'getUTCSeconds', 'valueOf', 
                        'setUTCMinutes', 'getUTCDay', 'toJSON', 'setUTCDate', 'setUTCSeconds', 'getYear', 'getUTCMilliseconds', 'getDay', 
                        'setFullYear', 'setMilliseconds', 'setTime', 'setHours', 'getSeconds', 'toGMTString', 'getDate', 'setUTCHours']
             },{
                 object: RegExp,
                 keys:     ['\$\*', '\$\`', '\$input', '\$\+', '\$\&', "\$\'", '\$\_']
             },{
                 object: RegExp.prototype,
                 keys: ['toString', 'exec', 'compile', 'test']
             },{
                 object: Error.prototype,
                 keys: ['toString']
             },{
                 object: Math,
                 keys: ['LN10', 'PI', 'E', 'LOG10E', 'SQRT2', 'LOG2E', 'SQRT1_2', 'LN2', 'cos', 'pow', 'log', 'tan', 'sqrt', 'ceil', 'asin', 
                        'abs', 'max', 'exp', 'atan2', 'random', 'round', 'floor', 'acos', 'atan', 'min', 'sin']
             },{
                 object: __global,
                 keys: ['TypeError', 'decodeURI', 'parseFloat', 'Number', 'URIError', 'encodeURIComponent', 'RangeError', 'ReferenceError', 
                        'RegExp', 'Array', 'isNaN', 'Date', 'Infinity', 'Boolean', 'Error', 'NaN', 'String', 'Function', 
                        'Math', 'undefined', 'encodeURI', 'escape', 'unescape', 'decodeURIComponent', 'EvalError', 'SyntaxError', 'Object', 
                        'eval', 'parseInt', 'JSON', 'isFinite']
             },{
                 test: function(obj){ return typeof JSON !== 'undefined' && obj === JSON; },
                 keys: ['stringify', 'parse']
             },{
                 test: function(obj){ return Array.isArray(obj) || __utils.isString(obj); },
                 keys: ['length']
             },{
                 test: function(obj){ return obj instanceof RegExp; },
                 testValue: new RegExp('.+'),
                 keys: ['lastIndex', 'multiline', 'global', 'source', 'ignoreCase']
             },{
                 test: function(obj){ return typeof obj === 'function' && obj.apply && obj.call; },
                 testValue: function(a,b,c){},
                 keys: ['arguments', 'length', 'name', 'prototype', 'caller']
             }
        ];
        
        for( var i=0, ilen=props.length; i < ilen; ++i){
            if( props[i].object ) {
                if( typeof props[i].object === 'function' ){
                    props[i].keys.push('arguments', 'length', 'name', 'prototype', 'caller');
                } else if( typeof props[i].object === 'object' && props[i].object !== Math && props[i].object !== __global ) {
                    props[i].keys.push('constructor');
                }
                for( var j=props[i].keys.length-1; j>=0; --j ) {
                    if( !(props[i].keys[j] in props[i].object) || __propertyIsEnumerable(props[i].object,props[i].keys[j]) ) {
                        props[i].keys.splice(j,1);
                    }
                }
            } else if( props[i].test && props[i].testValue && props[i].test(props[i].testValue) ) {
                for( var j=props[i].keys.length-1; j>=0; --j ) {
                    if( !(props[i].keys[j] in props[i].testValue) || __propertyIsEnumerable(props[i].testValue,props[i].keys[j]) ) {
                        props[i].keys.splice(j,1);
                    }
                }
                delete props[i].testValue;
            }
        }
        
        return props;
        
    })(); // __notEnumerableProperties



        
    /**
     * Binds the function to a given context and returns a wrapper function.
     * Practically it 'converts' a method to a function with remembering 
     * the context.
     * ECMAScript 5 Reference: 15.3.4.5
     * @name Function.prototype.bind
     * @info Binds the function to a given context and returns a wrapper function.
     * @function
     * @see $NS.es5
     * @param ctx {object} method's context
     * @returns {function} wrapped function
     * @example var flatFunction = obj.method.bind(obj);
     */
    Function.prototype.bind || (Function.prototype.bind = function(ctx){
        if( typeof  this !== 'function' ) {
            throw new TypeError( "'this' is not a function" );
        }
        var fn = this, 
            args = Array.prototype.slice.call(arguments,1);
            
        return function() {
            return fn.apply( ctx, args.concat(Array.prototype.slice.call(arguments)) );
        };
    });


    /**
     * Trims left and right side of the string. Method removes spaces, tabulators
     * and new line characters.
     * Method implements probably the fastest algorithm of JavaScript trim operation
     * (see http://blog.stevenlevithan.com/archives/faster-trim-javascript)
     * ECMAScript 5 Reference: 15.5.4.20
     * @name String.prototype.trim
     * @see $NS.es5
     * @function
     * @info Trims left and right side of the string.
     * @returns {string} trimmed string
     */
    String.prototype.trim || (String.prototype.trim = function(){
        return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    });


    /**
     *  ECMAScript 5 Reference: 15.4.3.2
     *  Tests if passed object is an Array
     *  @name Array.isArray
     *  @info Tests if passed object is an Array
     *  @see $NS.es5
     *  @function
     *  @since 1.0.1, revision 9 (thanks to dudleyflanders)
     *  @param obj object to be tested
     *  @returns {boolean} true if input parameter is an object false in any other case
     *  @example Array.isArray([]) === true;
     */
    Array.isArray || (Array.isArray = function(obj) {
        return Object.prototype.toString.call(obj) === "[object Array]" || (obj instanceof Array);
    });


    /**
     * ECMAScript 5 Reference: 15.4.4.14
     * According to specification Array.prototype.indexOf.length is 1
     * @name Array.prototype.indexOf
     * @info Finds an index of given element
     * @see $NS.es5
     * @function
     * @param searchElement - 
     * @param fromIndex {number} - start index (optional)
     * @returns {number} index of found element or -1
     * @example ['a','b','c'].indexOf('b') === 1;
     */
    $AP.indexOf || ($AP.indexOf = function(searchElement){
        var len = this.length,
            i = +arguments[1] || 0; // fromIndex
        
        if( len === 0 || isNaN(i) || i >= len ) {
            return -1;
        }
        
        if( i < 0 ) {
            i = len + i;
            i < 0 && (i = 0);
        }
        
        for( ; i < len; ++i ) {
            if( this.hasOwnProperty(String(i)) && this[i] ===  searchElement ) {
                return i;
            }
        }
        
        return -1;
    });


    /**
     * ECMAScript 5 Reference: 15.4.4.15
     * According to specification Array.prototype.lastIndexOf.length is 1
     * @name Array.prototype.lastIndexOf
     * @info Finds an index of given element starting from the end of an array
     * @see $NS.es5
     * @function
     * @param searchElement -
     * @param fromIndex {number} - start index (optional)
     * @returns {number} index of found element or -1
     * @example ['a','b','c'].indexOf('b') === 1;
     */
    $AP.lastIndexOf || ($AP.lastIndexOf = function(searchElement){
        var len = this.length,
            i = +arguments[1] || len-1; // fromIndex
        
        if( len === 0 || isNaN(i) ) {
            return -1;
        }
        
        if( i < 0 ) {
            i = len + i;
        } else if( i >= len ){
            i = len-1;
        }
        
        for( ; i >= 0; --i ) {
            if( this.hasOwnProperty(String(i)) && this[i] ===  searchElement ) {
                return i;
            }
        }
        
        return -1;
    });


    /**
     * If given callback returns true for all elements of the array, the method
     * itself returns true as well; false otherwise. 
     * ECMAScript 5 Reference: 15.4.4.16
     * @name Array.prototype.every
     * @info Tests if all array elements return true for a given callback
     * @see $NS.es5
     * @function
     * @param {function} callback a callback
     * @returns {boolean} true when callback returns true for all elements of 
     *             the array; false otherwise 
     * @throws {TypeError} when callback is not callable object
     * @see Array.prototype.some
     * @example var allEven = array.every(function(el){
     *                 return !(el & 1);
     *             });
     */
    $AP.every || ($AP.every = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }

        var thisArg = arguments[1]; 
        for(var i=0, len=this.length; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                if( !callback.call(thisArg, this[i], i, this) )
                    return false;
            }
        }

        return true;
    });


    /**
     * When callback returns true for at least one element of the array, then
     * the Array.prototype.some method returns true as well; false otherwise.
     * ECMAScript 5 Reference: 15.4.4.17
     * @name Array.prototype.some
     * @info Tests if at least one array element returns true for a given callback
     * @see $NS.es5
     * @function
     * @param {function} callback a callback
     * @returns {boolean} true when the callback returns true for at least one
     *             array element
     * @throws {TypeError} when callback is not callable object
     * @see Array.prototype.every
     * @example var containsNull = array.some(function(el){ return el===null });
     */
    $AP.some || ($AP.some = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }

        var thisArg = arguments[1]; 
        for(var i=0, len=this.length; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                if( callback.call(thisArg, this[i], i, this) ){
                    return true;
                }
            }
        }        
        
        return false;
    });


    /**
     * Invokes given callback function for each element of an array.
     * ECMAScript 5 Reference: 15.4.4.18
     * @name Array.prototype.forEach
     * @info Invokes callback for each element of an array
     * @see $NS.es5
     * @function
     * @param {function} callback a callback
     * @throws {TypeError} when callback is not callable object
     * @example [1,2,3].forEach(function(el){ console.log(el); });
     */
    $AP.forEach || ($AP.forEach = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }

        var thisArg = arguments[1]; 
        for(var i=0, len=this.length; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                callback.call(thisArg, this[i], i, this);
            }
        }        
    });


    /**
     * Invokes the callback for each element of an array and return the
     * array of callback results. The result array has the same length as 
     * input array.  
     * ECMAScript 5 Reference: 15.4.4.19
     * @name Array.prototype.map
     * @info Returns array of callback results for each element of an array.
     * @see $NS.es5
     * @function
     * @param {function} callback a callback
     * @returns {Array} array of callback results
     * @throws {TypeError} when callback is not a callable object
     * @example var squares = [1,2,3].map(function(n){return n*n;});
     */
    $AP.map || ($AP.map = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }

        var thisArg = arguments[1],
            len = this.length,
            results = new Array(len);
        for(var i=0; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                results[i] = callback.call(thisArg, this[i], i, this);
            }
        }
        
        return results;
    });


    /**
     * Invokes callback for each element of an array (starting from first one)
     * and returns array of those elements for which the callback returned true.
     * ECMAScript 5 Reference: 15.4.4.20
     * @name Array.prototype.filter
     * @info Returns sub-array of elements for which callback returned true
     * @see $NS.es5
     * @function
     * @param {function} callback a callback
     * @return {Array} an array of results
     * @throws {TypeError} when callback is not callable object
     * @example var odds = [1,2,3,4].filter(function(n){return n & 1; });
     */
    $AP.filter || ($AP.filter = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }

        var thisArg = arguments[1],
            len = this.length,
            results = [];
        for(var i=0; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                callback.call(thisArg, this[i], i, this) && results.push( this[i] );
            }
        }
        
        return results;
    });


    /**
     * Reduces an array to a single value. The callback is executed for each
     * element of an array starting from the first one. First argument of the
     * callback takes the result of previous callback invocation. For the first 
     * invocation either first element of an array is taken or the last (optional)
     * argument of the reduce method.
     * ECMAScript 5 Reference: 15.4.4.21
     * @name Array.prototype.reduce
     * @info Way to get single value from an array (ie. sum of all elements)
     * @see $NS.es5
     * @function
     * @requires __firstIndex
     * @param {function} callback a callback object
     * @returns {any} value of reduce algorithm; single value
     * @throws {TypeError} when callback is not a callable object
     * @see Array.prototype.reduceRight
     * @example var sum=[1,2,3].reduce(function(s,v){return s+v;}); 
     */
    $AP.reduce || ($AP.reduce = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }
        
        var len = this.length;
        if( len === 0 && arguments.length < 2 ) {
            throw new TypeError( "reduce of empty array with no initial value" );
        }
        
        var initIdx = -1;
        if( arguments.length < 2 ) {
            if( (initIdx = __firstIndex(this)) === -1 ) {
                throw new TypeError( "reduce of empty array with no initial value" );
            }
        }
        
        var val = arguments.length > 1 ? arguments[1] : this[initIdx];
        
        for(var i=initIdx+1; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                val = callback(val, this[i], i, this);
            }
        }
        
        return val;
    });


    /**
     * Works like Array.prototype.reduce, but starts from the end of an array.
     * ECMAScript 5 Reference: 15.4.4.22
     * @name Array.prototype.reduceRight
     * @info Like Array.prototype.reduce, but starts from the end of an array
     * @see $NS.es5
     * @function
     * @param {callable} callback function
     * @returns {any} value of reduce; single value
     * @throws {TypeError} when callback is not a callable object
     * @see Array.prototype.reduce
     * @example [10,20,30].reduceRight(function(a,b){return a-b;}) === 0
     */
    $AP.reduceRight || ($AP.reduceRight = function(callback){
        if( !__isCallable(callback) ) {
            throw new TypeError( callback + " is not a callable object" );
        }
        
        var len = this.length;
        if( len === 0 && arguments.length < 2 ) {
            throw new TypeError( "reduce of empty array with no initial value" );
        }
        
        var initIdx = len;
        if( arguments.length < 2 ) {
            for( var k=len-1; k >=0; --k ) {
                if( this.hasOwnProperty(String(k)) ) {
                    initIdx = k;
                    break;
                }
            }
            if( initIdx === len ) {
                throw new TypeError( "reduce of empty array with no initial value" );
            }
        }        
        
        var val = arguments.length > 1 ? arguments[1] : this[initIdx];
        
        for(var i=initIdx-1; i >= 0; --i) {
            if( this.hasOwnProperty(String(i)) ) {
                val = callback(val, this[i], i, this);
            }
        }
        
        return val;
    });


    /**
     * Numeric representation of current time (in milliseconds)
     * @name Date.now
     * @info Raturns current timestamp (as a number)
     * @see $NS.es5
     * @function
     * @returns {number} 
     * @example var timestamp = Date.now();
     * ECMAScript 5 Reference: 15.9.4.4
     */
    Date.now || (Date.now = function(){
        return +new Date();
    });


    /**
     * ECMAScript 5 Reference: 15.9.5.43
     * @name Date.prototype.toISOString
     * @info Returns date as ISO string
     * @see $NS.es5
     * @function
     * @returns {string}
     */
    Date.prototype.toISOString || (Date.prototype.toISOString = (function(){
        
        var str = function(n, l) {
                var str = String(n),
                    len = l || 2;
                while( str.length < len ) {
                    str = '0' + str;
                }
                return str;
            };
        
        return function(){
            return isFinite( this.getTime() )
                    ? String(this.getUTCFullYear()).concat( '-', 
                        str(this.getUTCMonth() + 1), "-",
                        str(this.getUTCDate()), "T",
                        str(this.getUTCHours()), ":",
                        str(this.getUTCMinutes()), ":",
                        str(this.getUTCSeconds()), ".",
                        str(this.getUTCMilliseconds(),3), "Z" )
                    : 'Invalid Date';
            };
        
    })() );


    /**
     * ECMAScript 5 Reference: 15.9.5.44
     * @name Date.prototype.toJSON
     * @info Returns string valid for JSON representation of date type
     * @see $NS.es5
     * @function
     */
    Date.prototype.toJSON || (Date.prototype.toJSON = function(key){ 
        if( !isFinite(this) ){ 
            return null;
        }
        if( !__utils.isFunction(this.toISOString) ) {
            throw new TypeError( "Date.prototype.toJSON called on incompatible " + (typeof this) );
        }
        
        return this.toISOString();
    });


    /**
     * Returns a prototype of an object. In this implementation the method tries to
     * use __proto__ attribute (for Spider/Trace-Monkey and Rhino) or constructor.prototype
     * reference which won't work for the overriden constructor property
     * ECMAScript 5 Reference: 15.2.3.2
     * @name Object.getPrototypeOf
     * @info Returns a prototype of an object 
     * @see $NS.es5
     * @function
     * @param obj {object} 
     * @returns {object} Object's prototype
     * @example Object.getPrototypeOf([]) === Array.prototype;
     */
    if( !Object.getPrototypeOf ) {
        if( "".__proto__ ) {
            Object.getPrototypeOf = function(obj) {
                if( !__utils.isObject(obj) ) {
                    throw new TypeError( obj + " is not an object" );
                }
                return obj.__proto__;
            };
        } else {
            Object.getPrototypeOf = function(obj) {
                if( !__utils.isObject(obj) ) { 
                    throw new TypeError( obj + " is not an object" );
                }
                return obj.constructor ? obj.constructor.prototype : null;
            };
        }
    }//,


    /**
     * Creates a new object with given prototype. The constructor of new object will point to its
     * prototype constructor. 
     * 
     * WARNING! When function called with second parameter it internally invokes Object.defineProperties method.
     * The implementation of this method provided in this library is not 100% valid with ECMAScript 5 specification
     * due to some limitations in ECMASCript 3. So in consequence also Object.create suffers from
     * limited functionality. For more details see description of Object.defineProperties method.
     * 
     * ECMAScript 5 Reference: 15.2.3.5
     * @name Object.create
     * @info Creates a new object with given prototype.
     * @see $NS.es5
     * @function
     * @param {object} proto a prototype of new object
     * @param {object} [properties] property descriptions - UNUSED in this implementation!
     * @returns new object with given prototype
     * @throws {TypeError} when proto is not an object
     * @example var newMe = Object.create( {me: 'test'} );
     * @see Object#defineProperties
     */
    Object.create || ( Object.create = (function(){

        // Moved outside the function to eliminate the closure memory effect
        var __TmpConstructor = function(){};
        
        return function(proto, properties) {
            if( !__utils.isObject(proto) ) { 
                throw new TypeError( proto + " is not an object" );
            }
            
            __TmpConstructor.prototype = proto;
            var obj = new __TmpConstructor();
            
            properties && Object.defineProperties( obj, properties );
            
            return obj;
        };
    })());


    /**
     * Checks whather the object structure is sealed with Object.seal or Object.freeze
     * methods. Because the implementation of these methods is impossible in ECMAScript 3,
     * this method always returns false.
     * ECMAScript 5 Reference: 15.2.3.11
     * @name Object.isSealed
     * @info Checks whether given object is sealed (by Object.seal method)
     * @see $NS.es5
     * @function
     * @param {object} obj an object to examine
     * @returns {boolean} always false
     * @throws {TypeError} when obj is not an object
     */
    Object.isSealed || ( Object.isSealed = function(obj){ 
        if( !__utils.isObject(obj) ) { 
            throw new TypeError( obj+" is not an object" );
        }
        return false; 
    });


    /**
     * Checks whether the object have been frozen with Object.freeze method.
     * Because the implementation of Object.freeze is impossible with ECMAScript 3 features,
     * the method always returns false.
     * ECMAScript 5 Reference: 15.2.3.12
     * @name Object.isFrozen
     * @info Checks whether given object is frozen (by Object.freeze method)
     * @see $NS.es5
     * @function
     * @param {object} obj an object to examine
     * @returns {boolean} always false
     * @throws {TypeError} when obj is not an object
     */    
    Object.isFrozen || ( Object.isFrozen = function(obj){
        if( !__utils.isObject(obj) ) { 
            throw new TypeError( obj+" is not an object" );
        }
        return false;         
    });


    /**
     * Checks whether the object structure can be extended. It returns false only when the object has
     * been protected by Object.preventExtensions, Object.seal or Object.freeze methods. 
     * Because in non-ECMAScript 5 interpreters there is not possible to provide such protection,
     * this implementation of Object.isExtensible always returns true. 
     * ECMAScript 5 Reference: 15.2.3.13
     * @name Object.isExtensible
     * @info  Checks whether given object is extensible.
     * @see $NS.es5
     * @function
     * @param {object} obj an object to examine
     * @returns {boolean} always true
     * @throws {TypeError} when obj is not an object
     */
    Object.isExtensible || ( Object.isExtensible = function(obj){ 
        if( !__utils.isObject(obj) ) { 
            throw new TypeError( obj+" is not an object" );
        }
        return true; 
    });


    /**
     * Returns property descriptor for property of a given object
     * ECMAScript 5 Reference: 15.2.3.3
     * @name Object.getOwnPropertyDescriptor
     * @info Returns property descriptor of given object.
     * @since 1.2
     * @see $NS.es5
     * @function
     * @param {object} obj an object
     * @param {string} pname property name to test; when it doesn't point to a valid property name
     *             the method return undefined
     * @returns {object} property descriptor or undefined
     * @throws {TypeError} when obj is null or not an object
     * @example Object.getOwnPropertyDescriptor(Array.prototype, "length");
     */
    Object.getOwnPropertyDescriptor || ( Object.getOwnPropertyDescriptor = (function(){
        
        var __NUMBER_CONSTS = ['MAX_VALUE', 'MIN_VALUE','NaN','POSITIVE_INFINITY','NEGATIVE_INFINITY'],
            __MATH_CONSTS = ['PI','E','LN2','LOG2E','LOG10E','SQRT1_2','SQRT2'];
        
        return function(obj, pname){
            if( !__utils.isObject(obj) ) { 
                throw new TypeError( obj+" is not an object" );
            }
            
            if( !(pname in obj) ) {
                return;
            }
            
            var editable = true,
                configurable = true;
            
            // recognize the only cases when ECMAScript 3 protects properties
            if( (obj===Number && __NUMBER_CONSTS.indexOf(pname)>=0) 
                    || (obj===Math && __MATH_CONSTS.indexOf(pname)>=0) 
                    || (pname=='length' && (obj===String.prototype || __utils.isString(obj) 
                    || obj===Function.prototype || obj instanceof Function)) ) {
                editable = false;
                configurable = false;
            } else if( pname=='length' && (obj===Array.prototype || Array.isArray(obj)) ) {
                configurable = false;
            } 
            
            return {
                writable: editable,
                enumerable: __propertyIsEnumerable(obj,pname),
                configurable: configurable,
                value: obj[pname]
            };
        };
    })());


    /**
     * Creates or redefines a property of an object. The property descriptor can contain one of
     * following attributes: value, writable, configurable, enumerable, get, set.
     * Get and set properties can't exist together with value or writable.
     * 
     * WARNING! The full implementation of defineProperty method is impossible with ECMAScript 3
     * features. In particular ECMAScript 3 does not allow to make properties non-enumerable, 
     * non-configurable or read only. As a consequence when at least on of these flags (enumerable,
     * configurable or writable) is set to false, the library will throw an Error.  
     * Also accessors (getters and setters) are not a part of ECMAScript 3 and as such they are not
     * supported by this library. 
     *  
     * ECMAScript 5 Reference: 15.2.3.6
     * @name Object.defineProperty
     * @info Defines/redefines property of a given object via property descriptor feature.
     * @see $NS.es5
     * @function
     * @requires __applyDefaults
     * @requires __toPropertyDescriptor
     * @since 1.2
     * @param {Object} obj an object
     * @param {string} property a property name
     * @param {Object} descriptor a property descriptor
     * @returns {Object} obj property modified by property descriptor
     * @throws {TypeError} when obj or descriptor is not an object or when property descriptor is 
     *             incorrect (i.e. contains both getter and value)
     * @example Object.defineProperty(myObj, "testValue", {
     *                 value:1, enumerable:true, writable:true, configurable:true});
     */
    Object.defineProperty || (Object.defineProperty = function(obj, property, descriptor){
        if( !__utils.isObject(obj) ) { 
            throw new TypeError( obj+" is not an object" );
        }
        
        var pname = String(property);
        var desc = __toPropertyDescriptor(descriptor);
        desc = __applyDefaults( desc, obj.hasOwnProperty(pname), obj[pname] );
        
        obj[pname] = desc.value;
        
        return obj;
    });


    /**
     * Creates or redefines properties of an object. Each element of 'properties' object
     * is a separate property descriptor. Each property descriptor can contain one of
     * following attributes: value, writable, configurable, enumerable, get, set.
     * Get and set properties can't exist together with value or writable.
     * When at least one of the property descriptors fail, all of the changes will be discarded.
     * 
     * WARNING! The full implementation of defineProperties method is impossible with ECMAScript 3
     * features. In particular ECMAScript 3 does not allow to make properties non-enumerable, 
     * non-configurable or read only. As a consequence when at least on of these flags (enumerable,
     * configurable or writable) is set to false, the library will throw an Error.  
     * Also accessors (getters and setters) are not a part of ECMAScript 3 and as such they are not
     * supported by this library. 
     * 
     * ECMAScript 5 Reference: 15.2.3.6
     * @name Object.defineProperties
     * @info Defines/redefines properties of a given object via property descriptor feature.
     * @see $NS.es5
     * @function
     * @requires __applyDefaults
     * @requires __toPropertyDescriptor
     * @since 1.2
     * @param {Object} obj an object
     * @param {Object} properties a map of property descriptors
     * @returns {Object} obj object modified with given property descriptors
     * @throws {TypeError} {TypeError} when obj or descriptor is not an object or when property descriptor is incorrect
     *             (i.e. contains both getter and value)
     * @example Object.defineProperty(myObj, { testValue: {
     *                 value:1, enumerable:true, writable:true, configurable:true}});
     */
    Object.defineProperties || (Object.defineProperties=function(obj, properties){
        if( !__utils.isObject(obj) ) { 
            throw new TypeError( obj+" is not an object" );
        }
        
        properties = Object( properties );
        var descriptors = {};
        for( var key in properties ) {
            if( properties.hasOwnProperty(key) ){
                var desc = __toPropertyDescriptor(properties[key]);
                descriptors[key] = __applyDefaults( desc, obj.hasOwnProperty(key), obj[key] );
            }
        }
        
        // when there are no error in property descriptors we can apply changes to the object
        for( key in descriptors ) {
            if( properties.hasOwnProperty(key) ){
                obj[key] = descriptors[key].value;
            }
        }
        
        return obj;
    });


    /**
     * Returns an array of object's own property names. It includes only the
     * enumerable properties. For all (enumerable and non-enumerable) properties
     * use Object.getOwnPropertyNames instead. 
     * ECMAScript 5 Reference: 15.2.3.14
     * @name Object.keys
     * @info Returns array of direct, enumerable object property names
     * @see $NS.es5
     * @function
     * @param obj {object} 
     * @returns {Array} array of own property names
     * @throws TypeError if the parameter is not an object
     * @example Object.keys({a:5}); // should return ["a"] 
     * @see Object#getOwnPropertyNames
     */     
    Object.keys || (Object.keys = function(obj){
        
        if( !__utils.isObject(obj) ) { 
            throw new TypeError( obj + " is not an object" );
        }
        
        var results = [];
        // key in obj is tricky here, but in IE global object doesn't have hasOwnProperty method
        for(var key in obj) {
            (obj.hasOwnProperty ? obj.hasOwnProperty(key) : key in obj) && results.push(key);
        }
        
        
        if( __utils.isString(obj) && !__features.STRING_INDEX_ENUMERABLE ) {
            for(var i=0, len=obj.length; i < len; ++i) {
                results.push( String(i) );
            }
        }
        
        return results;
    });


    /**
     * Returns an array of all direct property names of a given object - including the non-enumerable
     * properties. It makes the difference between this method and Object.keys. 
     * ECMAScript 5 reference: 15.2.3.4
     * @name Object.getOwnPropertyNames
     * @requires __notEnumerableProperties
     * @info Returns an array of all own property names of given object.
     * @see $NS.es5
     * @function
     * @since 1.2
     * @param {Object} obj an object
     * @returns {Array} Array of property names
     * @throws {TypeError} when obj is not an object
     * @see Object#keys
     */
    Object.getOwnPropertyNames || (Object.getOwnPropertyNames = function(obj){
        var keys = Object.keys(obj);
        for(var i=0, __len=__notEnumerableProperties.length; i < __len; ++i) {
            if( (__notEnumerableProperties[i].object && __notEnumerableProperties[i].object===obj) 
                    || (__notEnumerableProperties[i].test && __notEnumerableProperties[i].test(obj)) ) {
                keys = keys.concat( __notEnumerableProperties[i].keys );
                break;
            }
        }
        return keys;
    });



        
})();        


/**
 * Non-standard extensions of standard-prototypes. In contract to {@link fs.es5}
 * module which provides ECMAScript 5-compliant prototype extensions.  
 * 
 * Provided features:<ul>
 * <li>{@link Array#contains}</li>
 * <li>{@link Array#diff}</li>
 * <li>{@link Array#removeAll}</li>
 * <li>{@link Array#first}</li>
 * <li>{@link Array#invoke}</li>
 * <li>{@link Array#uniquePush}</li>
 * </ul>
 * 
 * @name fs.proto
 * @namespace Useful enhancements of standard prototypes
 * @static
 * @type {Object}
 */
(function(){

        var $AP = Array.prototype;

    
        
    /**
     * @name Array.prototype.contains
     * @info Checks whether array contains an element
     * @see $NS.proto
     * @nosideeffects
     * @function
     * @param elem
     * @returns {Boolean}
     */
    $AP.contains = function(elem) {
        for(var i=0, len=this.length; i < len; ++i){
            if( this[i] === elem ) {
                return true;
            }
        }
        return false;
    };


    /**
     * @name Array.prototype.diff
     * @requires Array.prototype.contains
     * @info Return difference between two arrays
     * @see $NS.proto
     * @nosideeffects
     * @function
     * @param arr
     * @returns {Array}
     */
    $AP.diff = function(arr){
        var result = [];
        for(var i=0, len=this.length; i < len; ++i){
            !arr.contains(this[i]) && result.push(this[i]);
        }
        return result;
    };


    /**
     * @name Array.prototype.removeAll
     * @info Removes from an array elements for which callback function returned true
     * @see $NS.proto
     * @function
     */
    $AP.removeAll = function(item) {
        var found = 0;
        
        if( this.length < 1 ) {
            return;
        }
        
        for(var i=this.length-1; i >= 0; --i) {
            if(this[i] === item) {
                this.splice(i,1);
                ++found;
            }
        }
        
        return found;
    };


    /**
     * @name Array.prototype.first
     * @info Returns first element for which callback function returned true
     * @see $NS.proto
     * @nosideeffects
     * @function
     */
    $AP.first = function(callback){
        
        if( !__utils.isFunction(callback) ) {
            throw new TypeError( callback + " is not a function" );
        }
    
        var thisArg = arguments[1]; 
        for(var i=0, len=this.length; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                if( callback.call(thisArg, this[i], i, this) ) {
                    return this[i];
                }
            }
        }
    
        return void 0;
    };


    /**
     * @name Array.prototype.invoke
     * @info Invokes method of each array element and returns an array of results
     * @see $NS.proto
     * @function
     * @example [[1,2,3],["a","b"]].invoke("join",""); //returns ["123","ab"]
     */
    $AP.invoke = function(methodName, param1 /*,...*/){
        var args = $AP.slice.call(arguments, 1),
            len = this.length,
            results = new Array(len);
        
        for(var i=0; i < len; ++i) {
            if( this.hasOwnProperty(String(i)) ) {
                results[i] = this[i][methodName].apply(this[i], args);
            }
        }
        
        return results;        
    };


    /**
     * @name Array.prototype.uniquePush
     * @info Push element to an array only if it doesn't already exits there
     * @see $NS.proto
     * @requires Array.prototype.contains 
     * @function
     */
    $AP.uniquePush = function(elem){
        if( !this.contains(elem) ) {
            this.push(elem);
            return true;
        }
        return false;
    };




    
})();


// fs.env module
$NS.env = (function(){

    /**
     * @name __E4X_SUPPORT
     * @type boolean
     * @private
     */
    var __E4X_SUPPORT = (function(){
            try {
                var test = eval("<a href='#'>test</a>");
            } catch(ex){}
            return typeof test === 'xml';
        })();


    /**
     * @name __STRICT_MODE_SUPPORT
     * @type boolean
     * @private
     */
    var __STRICT_MODE_SUPPORT = (function(){
            "use strict";
            return !this; 
        })();


    /**
     * List of features available in all versions of ECMAScript
     * @name __ES
     * @static
     * @private
     */
    var __ES = {
            
            // ECMAScript 2 features
            ES2: [
                  {
                      object: __global,
                      keys: ["parseFloat","isNaN","Infinity","NaN","escape",
                             "unescape","eval","parseInt","isFinite","Object","Function",
                             "Array","String","Boolean","Number", "Date", "Math"]
                  },{
                      object: Object.prototype,
                      keys: ["toString","constructor","valueOf"]
                  },{
                      object: Function.prototype,
                      keys: ["toString","constructor"]
                  },{
                      object: Array.prototype,
                      keys: ["constructor","toString","join","reverse","sort"]
                  },{
                      object: String,
                      keys: ["fromCharCode"]
                  },{
                      object: String.prototype,
                      keys: ["constructor","toString","valueOf","charAt","charCodeAt","indexOf",
                             "lastIndexOf","split","substring","toLowerCase","toUpperCase"]
                  },{
                      object: Boolean.prototype,
                      keys: ["constructor","toString","valueOf"]
                  },{
                      object: Number,
                      keys: ["MAX_VALUE","MIN_VALUE","NaN","NEGATIVE_INFINITY","POSITIVE_INFINITY"]
                  },{
                      object: Number.prototype,
                      keys: ["constructor","toString","valueOf"]
                  },{
                      object: Date,
                      keys: ["UTC","parse"]
                  },{
                      object: Date.prototype,
                      keys: ["constructor","toString","toDateString","toTimeString","toLocaleString",
                             "valueOf","getTime","getYear","getFullYear","getUTCFullYear","getMonth","getUTCMonth","getDate",
                             "getUTCDate","getDay","getUTCDay","getHours","getUTCHours","getMinutes",
                             "getUTCMinutes","getSeconds","getUTCSeconds","getMilliseconds",
                             "getUTCMilliseconds","getTimezoneOffset","setTime","setMilliseconds",
                             "setUTCMilliseconds","setSeconds","setUTCSeconds","setMinutes",
                             "setUTCMinutes","setHours","setUTCHours","setDate","setUTCDate","setMonth",
                             "setUTCMonth","setFullYear","setUTCFullYear","toUTCString","toGMTString","setYear"]
                  },{
                      object: Math,
                      keys: ["E","LN10","LN2","LOG2E","LOG10E","PI","SQRT1_2","SQRT2","abs","acos","asin",
                             "atan","atan2","ceil","cos","exp","floor","log","max","min","pow","random",
                             "round","sin","sqrt","tan"]
                  }
               ],

            
            // ECMAScript 3 features
            ES3: [
                    {
                        object: __global,
                        keys: ["decodeURI","encodeURIComponent","undefined","encodeURI","decodeURIComponent",
                               "RegExp","Error","EvalError","RangeError","ReferenceError","SyntaxError",
                               "TypeError","URIError"]
                    },{
                        object: Object.prototype,
                        keys: ["toLocaleString","hasOwnProperty","isPrototypeOf","propertyIsEnumerable"]
                    },{
                        object: Function.prototype,
                        keys: ["call","apply"]
                    },{
                        object: Array.prototype,
                        keys: ["toLocaleString","concat","pop","push","shift","slice","splice","unshift"]
                    },{
                        object: String.prototype,
                        keys: ["concat","localeCompare","match","replace","search","slice",
                               "toLocaleLowerCase","toLocaleUpperCase"]
                    },{
                        object: Number.prototype,
                        keys: ["toLocaleString","toFixed","toExponential","toPrecision"]
                    },{
                        object: Date.prototype,
                        keys: ["toDateString","toTimeString","toLocaleDateString","toLocaleTimeString"]
                    },{
                        object: typeof RegExp === 'function' ? RegExp.prototype : null,
                        keys: ["exec","test","toString"]
                    },{
                        object: typeof Error === 'function' ? Error.prototype : null,
                        keys: ["name","message","toString"]
                    }
                 ],
                 
            
            // ECMAScript 5 features     
            ES5: [
                      {
                          object: __global,
                          keys: ["JSON"]
                      },{
                          object: Object,
                          keys: ["getOwnPropertyNames","getPrototypeOf","keys","create","preventExtensions",
                                 "seal","freeze","isSealed","isFrozen","isExtensible","defineProperty",
                                 "defineProperties","getOwnPropertyDescriptor"]
                      },{
                          object: Function.prototype,
                          keys: ["bind"]
                      },{
                          object: Array,
                          keys: ["isArray"]
                      },{
                          object: Array.prototype,
                          keys: ["indexOf","lastIndexOf","every","some","forEach","filter","map",
                                 "reduce","reduceRight"]
                      },{
                          object: String.prototype,
                          keys: ["trim"]
                      },{
                          object: Date.prototype,
                          keys: ["toISOString","toJSON"]
                      }
                 ],
                 
                 
            hasAllFeatures: function(features) {
                    for(var i=0, ilen=features.length; i < ilen; ++i){
                        var obj =  features[i].object;
                        if( !obj ) {
                            return false;
                        }
                        for(var j=0, jlen=features[i].keys.length; j < jlen; ++j){
                            if( obj.hasOwnProperty ? !obj.hasOwnProperty(features[i].keys[j]) : (typeof obj[features[i].keys[j]] !== 'undefined') ) {
                                return false;
                            }
                        }
                    }
                    return true;
                },
                
                
            findFeature: function(testObj, feature) {
                for(var key in this) {
                    if( typeof this[key].splice !== 'function' || !this.hasOwnProperty(key) ) {
                        continue;
                    }
                    var features = this[key];
                    for(var i=0, ilen=features.length; i < ilen; ++i){
                        var obj =  features[i].object;
                        if( obj !== testObj ) {
                            continue;
                        } 
                        for(var j=0, jlen=features[i].keys.length; j < jlen; ++j){
                            if( features[i].keys[j] === feature ) {
                                return +key.charAt(key.length-1);
                            }
                        }
                    }
                }
                return -1;                
            }
                
        };



    
    return {
        
        /**
         * Checks whether Document Object Model is supported
         * @name fs.env.DOM
         * @info Checks whether Document Object Model is supported.
         * @type boolean
         * @constant
         */    
        DOM: !!( typeof window !== 'undefined' && typeof document !== 'undefined' && typeof document.getElementById !== 'undefined' ),


        /**
         * True whether CommonJS functionality is supported. 
         * @name fs.env.COMMON_JS
         * @info True whether CommonJS functionality is supported.
         * @type boolean
         * @constant
         */
        COMMON_JS: !!(typeof require === 'function' && typeof module === 'object' ),


        /**
         * @name fs.env.RHINO
         * @info True if application runs under Rhino.
         * @type boolean 
         * @constant
         */
        RHINO: !!( typeof java === 'object' && java.lang && java.lang.Array && java.util 
                && java.util.HashMap && typeof loadClass === 'function' ),


        /**
         * @name fs.env.NODE_JS
         * @info True if application runs under Node.JS
         * @type boolean
         * @constant
         */
        NODE_JS: !!( typeof process === 'object' && process.on && typeof GLOBAL === 'object' 
                && GLOBAL.setTimeout && typeof require === 'function' ),


        /**
         * Indicates whether ECMAScript for XML is supported by the JavaScript engine.
         * @name fs.env.E4X
         * @info Indicates whether ECMAScript for XML is supported by the JavaScript engine.
         * @requires __E4X_SUPPORT
         * @type boolean
         * @constant
         */
        E4X: __E4X_SUPPORT,


        /**
         * @name fs.env.JS_VERSION
         * @info Returns JavaScript version.
         * @type string
         * @requires __E4X_SUPPORT
         * @requires __STRICT_MODE_SUPPORT
         * @requires __ES
         * @constant
         */
        JS_VERSION: (function(){
                var version = (function(){
                    
                    // JS 1.8 test
                    try {
                        var expressionClosure = eval("(function(x) x*x)");
                    } catch(ex) {}
                    
                    if( typeof expressionClosure === 'function' ) {
                        if( typeof JSON === 'object' && !JSON.propertyIsEnumerable("parse") ){
                            return __STRICT_MODE_SUPPORT ? "1.8.5" : "1.8.1";
                        }
                        return "1.8";
                    }

                    // JS 1.7 test
                    try {
                        var letExpression = eval("10*let(x=5) x");
                    } catch(ex){}
                    
                    if( letExpression === 50 ) {
                        return "1.7";
                    }
                    
                    // JS 1.6 test
                    if( __E4X_SUPPORT ) {
                        return "1.6";
                    }
                    
                    if( __ES.hasAllFeatures(__ES.ES3) ) {
                        return "1.5";
                    }
                    
                    // JS1.4 recognition skipped - no new features in comparison to 1.3  
                    // apart support for Netscape Server
                    
                    if( [0,1,2].push(3) === 4 ) {
                        return "1.3";
                    }
                    
                    if( typeof RegExp === 'function' && String.prototype.concat ) {
                        return "1.2";
                    }
                    
                    if( Object.prototype.valueOf && Object.prototype.constructor ) {
                        return "1.1";
                    }
                    
                    return "1.0";
                })(); 
                
                return version;
                
            })(),


        /**
         * Checks if version of current JS engine is smaller or equal to given attribute
         * @name fs.env.isVersion
         * @info Checks if version of current JS engine is smaller or equal to given attribute
         * @requires fs.env.JS_VERSION
         * @nosideeffects
         * @function
         * @param {string} Minimal expected JavaScript version
         * @returns {boolean}
         * @example
         * if(!fs.env.isJSVersion("1.6")) {
         *    alert( "JavaScript in version at least 1.6 is required!" );
         * }
         */    
        isJSVersion: function(version){
                return this.JS_VERSION >= version;
            },


        /**
         * ECMAScript version of current JavaScript engine 
         * @name fs.env.ES_VERSION
         * @info ECMAScript version of current JavaScript engine 
         * @type number
         * @requires __ES
         * @constant
         */
        ES_VERSION: (function(){
                
                if( __ES.hasAllFeatures(__ES.ES2) ){
                    if( __ES.hasAllFeatures(__ES.ES3) ) {
                        if( __ES.hasAllFeatures(__ES.ES5) && __STRICT_MODE_SUPPORT ) {
                            return 5;
                        } else {
                            return 3;
                        }
                    } else {
                        return 2;
                    }
                }
            
                return -1;
                
            })(),


        /**
         * True when the JavaScript engine supports ECMAScript 5 features (without strict mode);
         * false otherwise.
         * @name fs.env.ECMA_SCRIPT_5_METHODS
         * @info True if JavaScript engine supports all ECMAScript 5 methods (without strict mode).
         * @type boolean 
         * @requires __ES
         * @constant
         */
        ECMA_SCRIPT_5_METHODS: __ES.hasAllFeatures(__ES.ES5),


        /**
         * True if the JavaScript engine supports the strict mode; false otherwise
         * @name fs.env.STRICT_MODE
         * @info True if strict mode is supported.
         * @type boolean
         * @constant
         */
        STRICT_MODE: __STRICT_MODE_SUPPORT,


        /**
         * True when the JavaScript engine supports ECMAScript 5 features (without strict mode);
         * false otherwise.
         * @name fs.env.ECMA_SCRIPT_5
         * @info True if current JavaScript engine supports all ECMAScript 5 features
         * @type boolean 
         * @requires __ES
         * @constant
         */        
        ECMA_SCRIPT_5: __ES.hasAllFeatures(__ES.ES5) && __STRICT_MODE_SUPPORT,


        /**
         * Returns version of JavaScript/ECMAScript when feature was introduced
         * @name fs.env.featureOf
         * @info Returns version of JavaScript/ECMAScript when feature was introduced
         * @requires __ES
         * @nosideeffects
         * @function
         * @param {object} obj
         * @param {string} feature
         * @returns {object}
         */
        getFeatureVersion: function(obj, feature){
            if( !__utils.isObject(obj) ) {
                throw new TypeError( obj+" is not an object" );
            }
            return {
                ES: __ES.findFeature(obj, feature)
            };
        },


        /**
         * Returns descriptive information about actual JavaScript environment.
         * It can produce text like this:
         * "JavaScript version 1.8.1 with ECMAScript 5 support"
         * @name fs.env.toString
         * @info Descriptive information about current JavaScript environment.
         * @nosideeffects
         * @function
         * @returns {string} environment description
         */
        toString: function(){
            return "JavaScript version ".concat( this.JS_VERSION, 
                    " with ECMAScript ", this.ECMA_SCRIPT_5 ? "5" : "3", " support" );
        }




    };
    
})();




    // Provide the library via environment-dependent method
    (function(){
        if (typeof define === "function" && typeof require === "function" && typeof require.onScriptLoad === "function") {
            // RequireJS
            define( $NS );
        } else if( typeof require === "function" && typeof exports === "object" ) {
            // CommonJS
            __utils.mixin( exports, $NS );
        } else if( typeof __FUNCTION_SACK_CONFIG__ === "object" && (__global || __FUNCTION_SACK_CONFIG__.context) ) {
            // browser script tag, Rhino, SpiderMonkey, etc (with dedicated config object)
            (function(config){
                var ctx = config.context || __global;
                if( __utils.isFunction(ctx) ) {
                    return void ctx( $NS );
                }
                if( !!config.ignoreRootNamespace ) {
                    return void __utils.mixin( ctx, $NS );
                }
                ctx[ __utils.isString(config.rootNamespaceName) ? config.rootNamespaceName : "fs" ] = $NS;
            })(__FUNCTION_SACK_CONFIG__);
        } else if( __global ) {
            // browser script tag, Rhino, SpiderMonkey, etc
            __global.fs = $NS;
        } else {
            // shouldn't take place unless someone plays with the code or with the context
            throw new Error( "Unspecified FunctionSack load method" );
        }
    })();

})(this);

// Generated on 2011-08-28 23:33:16.042593
