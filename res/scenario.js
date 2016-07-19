/**
 * Created by geoffrey on 25/05/2016.
 */


(function () {
    var links = document.links;
    for(var i = 0; i<links.length; i++){ //compat ie < 10
        var link = links[i];
        link.target = "_blank";
    }

    var gifOverlayWrapper = document.getElementById("overlay");
    var gifOverlay = document.getElementById("gif-overlay");

    var gifs = document.getElementsByClassName("gif");

    var onGifClick = function (e) {
        gifOverlay.src = e.target.src;
        gifOverlayWrapper.style.display = "block";
    };

    for(var i = 0; i<gifs.length; i++){
        var gif = gifs[i];
        gif.onclick = onGifClick;
    }

    gifOverlayWrapper.onclick = function () {
        gifOverlayWrapper.style.display = "none";
    };

})();
