'use strict';

/**
 * Modal registration controller.
 */
angular.module('docs').controller('ModalRegistration', function($scope, $uibModalInstance, Restangular, $translate) {
  $scope.user = {
    username: '',
    email: '',
    password: '',
    passwordConfirm: ''
  };

  /**
   * Close the modal.
   */
  $scope.close = function() {
    $uibModalInstance.dismiss('cancel');
  };

  /**
   * Register.
   */
  $scope.register = function() {
    Restangular.one('registrationrequest').put({
      username: $scope.user.username,
      email: $scope.user.email,
      password: $scope.user.password
    }).then(function() {
      $uibModalInstance.close($scope.user.username);
    }, function(data) {
      if (data.data.type === 'AlreadyExistingUsername') {
        $scope.error = 'register.error.alreadyexistingusername';
      } else {
        $scope.error = 'register.error.unknown';
      }
    });
  };
});