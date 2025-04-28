'use strict';

/**
 * Settings register request controller.
 */
angular.module('docs').controller('SettingsRegister', function($scope, Restangular, $state, $dialog, $translate) {
  // Loading state
  $scope.loading = true;
  
  /**
   * Load registration requests.
   */
  $scope.loadRequests = function() {
    Restangular.one('registrationrequest').get().then(function(data) {
      $scope.loading = false;
      
      // Separate pending from processed requests
      $scope.pendingRequests = data.requests.filter(function(request) {
        return request.status === 'PENDING';
      });
      
      $scope.historyRequests = data.requests.filter(function(request) {
        return request.status !== 'PENDING';
      });
    });
  };
  
  // Load requests on init
  $scope.loadRequests();
  
  /**
   * Approve a registration request.
   */
  $scope.approve = function(request) {
    var title = $translate.instant('settings.register.approve_title');
    var msg = $translate.instant('settings.register.approve_message', { username: request.username });
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('registrationrequest', request.id).post('approve').then(function() {
          $scope.loadRequests();
        }, function(e) {
          var errorMsg = '';
          if (e.data.type === 'AlreadyProcessed') {
            errorMsg = $translate.instant('settings.register.error_already_processed');
          } else {
            errorMsg = $translate.instant('settings.register.error_unknown');
          }
          
          var title = $translate.instant('settings.register.error_title');
          var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
          $dialog.messageBox(title, errorMsg, btns);
        });
      }
    });
  };
  
  /**
   * Reject a registration request.
   */
  $scope.reject = function(request) {
    var title = $translate.instant('settings.register.reject_title');
    var msg = $translate.instant('settings.register.reject_message', { username: request.username });
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-danger' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('registrationrequest', request.id).post('reject').then(function() {
          $scope.loadRequests();
        }, function(e) {
          var errorMsg = '';
          if (e.data.type === 'AlreadyProcessed') {
            errorMsg = $translate.instant('settings.register.error_already_processed');
          } else {
            errorMsg = $translate.instant('settings.register.error_unknown');
          }
          
          var title = $translate.instant('settings.register.error_title');
          var btns = [{ result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }];
          $dialog.messageBox(title, errorMsg, btns);
        });
      }
    });
  };
});