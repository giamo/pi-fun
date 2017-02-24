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
            var playlist = data.playlist;
            result.playlist = playlist;
            result.currentSong = {};

            if (playlist.current !== undefined) {
                var curr = playlist.songs[playlist.current];
                result.currentSong.title = curr.title;

                if (curr.artist !== undefined) {
                    result.currentSong.artist = curr.artist;
                }

                if (curr.length !== undefined) {
                    result.currentSong.length = curr.length;
                }

                if (curr.elapsed !== undefined) {
                    result.currentSong.elapsed = curr.elapsed;
                }

                if (curr.cover !== undefined) {
                    result.currentSong.cover = curr.cover;
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
        return $http.post('/api/player/playpause')
            .success(function(data) {
            }).error(function(err) {
                console.log('Error playing/pausing player audio: ' + err);
            });
    };

    this.updatePlayingStatus = function(state) {

    }

    this.play = function(index) {
        var deferred = $q.defer();
        var result = {};

        $http.post('/api/player/play/' + index).success(function(data) {

        });
    }
}]);