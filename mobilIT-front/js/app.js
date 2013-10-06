'use strict';

/* App Module */
var app = angular.module('mobilIT', ['Services', 'Filter']);

app.config(function ($routeProvider) {
    $routeProvider
        .when('/', {templateUrl: 'partials/form.html', controller: FormCtrl})
        .when('/error', {templateUrl: 'partials/error.html', controller: ErrorCtrl})
        .otherwise({redirectTo: '/'});
});

app.run(function ($rootScope, $location) {
});