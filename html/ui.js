var ui = (function() {
  "use strict";

  // key code 189 corresponds to minus
  // key code 187 corresponds to equals
  // key code 48 corresponds to zero
  // assumes that javaWebView is the JavaFX webView object responsible
  // for displaying the page
  var handleZoomEvents = function(e) {
    if (e.ctrlKey) {
      var currentZoom = window.javaWebView.getZoom();
      switch (e.keyCode) {
        case 48:
          window.javaWebView.setZoom(1);
          break;
        case 187:
          window.javaWebView.setZoom(currentZoom + 0.1);
          break;
        case 189:
          window.javaWebView.setZoom(currentZoom - 0.1);
          break;
        default:
          break;
      }
    }
  };

  function enableZooming() {
    window.onkeydown = handleZoomEvents;
  }

  function disableZooming() {
    window.onkeydown = null;
  }

  return {
    enableZooming: enableZooming,
    disableZooming: disableZooming
  };
})();
