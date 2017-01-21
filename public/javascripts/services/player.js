var app = angular.module('pi');

app.service('PlayerApi', ['$q', '$http', function($q, $http) {
    this.getStatus = function() {
        var deferred = $q.defer();
        var result = {};

        $http.get('/api/player/status').success(function(data) {
            var isError = (data !== undefined) ? data.error : true;

            if (isError !== undefined && isError === true) {
                result.playerUnreachable = true;
                deferred.resolve(result);
                return;
            } else {
                result.playerUnreachable = false;
            }

            var state = data.state;

            if (data.metadata !== undefined) {
                var meta = data.metadata;
                result.currentSong.title = meta.title;

                if (meta.artist !== undefined) {
                    result.currentSong.artist = meta.artist;
                }

                if (meta.length !== undefined) {
                    result.currentSong.length = meta.length;
                }

                if (meta.cover !== undefined) {
                    result.currentSong.cover = meta.cover;
                }
            }

            if (state === 'playing') {
                result.isPlaying = true;
            } else {
                result.isPlaying = false;
            }

            deferred.resolve(result);
        }).error(function(err) {
            console.log('Error getting player status: ' + err);
            result.playerUnreachable = true;
            deferred.resolve(false);
        });

        return deferred.promise;
    };

    this.playPause = function() {
        // var deferred = $q.defer();
        // var result = {};
        return $http.get('/api/player/playpause')
            .success(function(data) {
                // if (state === 'playing') {
                //     result.isPlaying = true;
                // } else {
                //     result.isPlaying = false;
                // }
                // deferred.resolve(result);
            }).error(function(err) {
                console.log('Error playing/pausing player audio: ' + err);
                // deferred.resolve(false);
            });
    };

    this.updatePlayingStatus = function(state) {

    }
}]);