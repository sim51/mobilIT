'use strict';

/* Filters */
angular.module('Filter', [])
    .filter('co2Car', function() {
        return function(input) { return Math.round10(input * 0.0812, -2);};
    })
    .filter('time', function() {
        return function(input) { return Math.round(input * 60);};
    });
