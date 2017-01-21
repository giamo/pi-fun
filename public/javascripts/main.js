angular.module('pi', ['ngResource', 'ngRoute'])
  .config(["$routeProvider", function($routeProvider) {
    return $routeProvider.when('/', {
      templateUrl: 'views/home',
      controller: 'AppCtrl'
    }).when('/home', {
      templateUrl: 'views/home',
      controller: 'AppCtrl'
    }).when('/music', {
      templateUrl: 'views/music',
      controller: 'MusicCtrl'
    }).otherwise({
      redirectTo: '/'
    });
  }])
  .config(["$locationProvider", function($locationProvider) {
    return $locationProvider.html5Mode(true).hashPrefix("!");
  }]);
