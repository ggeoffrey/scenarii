/**
 * Created by geoffrey on 25/05/2016.
 */


(function () {
    var links = document.links;
    for(var i = 0; i<links.length; i++){ //compat ie < 10
        var link = links[i];
        //if(link.hostname != window.location.hostname){
        link.target = "_blank"
        //}
    }
})();
