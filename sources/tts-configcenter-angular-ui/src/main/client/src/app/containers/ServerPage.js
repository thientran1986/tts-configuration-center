class ServerPageController {
  /** @ngInject */
  constructor($rootScope, $scope, $stateParams, loggingService, zoneService, serverService) {
    this.rootScope = $rootScope;
    this.scope = $scope;
    this.loggingService = loggingService;
    this.zoneService = zoneService;
    this.serverService = serverService;
    this.stateParams = $stateParams;
    // URL Params
    if ($stateParams !== null) {
      this.stateParams = $stateParams;
      this.initialize($stateParams.zoneId, $stateParams.serverId);
    }

    // Register event for reloading new data
    this.rootScope.$on('reloadZoneInfoLeft', () => {
      this.initialize(this.stateParams.zoneId, this.stateParams.serverId);
    });
    // Unregister listener on root if controller is destroyed
    $scope.$on('$destroy', () => {
      this.rootScope.$$listeners.reloadZoneInfoLeft = [];
    });
  }

  initialize(zoneId, serverId) {
    return this.zoneService.loadZones(zoneId, fetchedZones => {
      this.serverService.findByZones(fetchedZones, zoneAndServers => {
        this.zoneAndServers = zoneAndServers;
        this.zone = zoneAndServers[0];
        for (const server of this.zone.servers) {
          if(server.id == serverId) {
            this.server = server;
          }
        }
      });
    });
  }
}

angular
  .module('app')
  .component('serverPage', {
    templateUrl: 'app/containers/ServerPage.html',
    controller: ServerPageController
  });
