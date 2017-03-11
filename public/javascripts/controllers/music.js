var app = angular.module('pi');

app.controller('MusicCtrl', [ '$scope', '$interval', 'PlayerApi', function($scope, $interval, PlayerApi) {

    $scope.player = PlayerApi;
    $scope.defaultCover = '/assets/images/raspberry_pi_logo.png';

    // get status info every 500 milliseconds
    $interval(function() {
        $scope.getAudioStatus();
    }, 500);

    $scope.getAudioStatus = function() {
        $scope.player.getStatus().then(function(result) {
            $scope.playerUnreachable = (result.playerUnreachable !== undefined) ? result.playerUnreachable : true;
            $scope.isPlaying = (result.isPlaying !== undefined) ? result.isPlaying : false;
            $scope.currentSong = (result.currentSong !== undefined) ? result.currentSong : {};
            $scope.currentSongId = (result.playlist !== undefined) ? result.playlist.current : {};
            $scope.playlist = result.playlist;
        });
    };

    $scope.mustHighlight = function(song) {
        if (song === null) {
            return false;
    	}

        return song.id == $scope.currentSongId;
     };

     $scope.selectCurrentSong = function(thisSong) {
        $scope.currentSongId = thisSong.id;
     };

     $scope.playNext = function() {
        $scope.player.play($scope.currentSongId + 1);
     };

     $scope.playPrevious = function() {
        $scope.player.play($scope.currentSongId - 1);
     };

     $scope.submitAdd = function(path) {
        $scope.player.addToQueue(path);
     }
}]);
