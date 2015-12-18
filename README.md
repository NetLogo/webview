# WebView JavaFX Supplemental Extension

## Javascript API

The javascript API is provided by an object implementing `JavascriptBridge`.
It is available only to html pages on the filesystem (http and https pages cannot access it).
It provides the following javascript methods:

* `NetLogo.evaluateCommand(commandText, onCompletion, onError)`

  returns `undefined`.

  * `commandText` - The text of the command to be evaluated
  * `onCompletion` - zero-argument function called on successful completion
  * `onError` - one-argument function called with a java Exception on error

* `NetLogo.evaluateReporter(reporterText, onResult, onError)`

  returns `undefined`.

  * `reporterText` - The text of the reporter to be evaluated
  * `onResult` - one-argument function called on successful completion with the result of the procedure. This can be any of the following:
    1. A string
    2. A boolean
    3. A LogoList
    4. A number (whose numeric value must be obtained with `doubleValue()`. See `html/netlogo.js` for an example.
  * `onError` - one-argument function called with a java Exception on error

* `NetLogo.log(message)` and `NetLogo.err

  returns `undefined`.

  * `message` - A string which will be written to `System.out`.

## Primitives

### `webview:create-tab`

Start a new tab for webview. Will open as the farthest right tab.
Optionally accepts a tab name and a document to open.
If a tab or frame is open, an exception will be thrown.

Example:
```NetLogo
(webview:create-tab)                                 ; creates a tab at far right with name "Webview", open to "html/index.html"
webview:create-tab "Extra Tab Fun!"                  ; creates a tab with name "Extra Tab Fun!"
webview:create-tab "Test" "test2.html"               ; creates a tab with name "Test", open to "html/test2.html"
webview:create-frame "Test" "http://www.example.org" ; creates a tab with title "Test", open to "example.org"
```

### `webview:create-frame`

Open a new frame for webview.
Optionally accepts a tab name and document to open.
If a tab or frame is open, an exception will be thrown.

Example:
```NetLogo
(webview:create-frame)                               ; creates a frame at far right with title "Webview", open to "html/index.html"
webview:create-frame "Extra Frame Fun!"              ; creates a frame with title "Extra Frame Fun!"
webview:create-frame "Test" "test2.html"             ; creates a frame with title "Test", open to "html/test2.html"
webview:create-frame "Test" "http://www.example.org" ; creates a frame with title "Test", open to "example.org"
```

### `webview:browse`

Directs the system-default browser to open the specified URL.
The browser will launch in a separate process.
Consequently, further calls to webview primitives will not effect this browser.

Example:
```NetLogo
webview:browse "htttp://www.example.org/" ; direct the operating system to open the default browser to example.org
```

### `webview:load`

Points an open webview frame to a URL.

Example:
```NetLogo
webview:load "http://www.google.com
```

### `webview:reload`

Reloads the current page (useful for if the webview gets stuck).

Example:
```NetLogo
webview:reload
```

### `webview:close`

Closes the tab or frame currently associated with webview.
If no tab or frame is currently open, does nothing.

Example:
```NetLogo
webview:close
```

### `webview:exec`

Execute javascript within the context of the document.
If webview has not been started before this call, an error will be raised.

Example:

```NetLogo
webview:exec "document.body.appendChild(document.createTextNode('hello!'));"
```

### `webview:eval`

Execute javascript within the context of the document, waiting for a result to be ready.

Example:

```NetLogo
webview:eval "2 + 2"                                       ; => 4
webview:eval "[\"abc\", 123, false]"                       ; => (list "abc" 123 false)
webview:eval "var o = {\"abc\": 123, \"def\": false}; o"   ; => (list (list "abc" 123 (list "def" false))
webview:eval "document.getElementById(\"reporter\").value" ; => "foo"
```

### `webview:add-module`

Adds a predefined webview "module" to the current page.
Note that the "NetLogo" and "javaWebView" modules are added by default on all pages loaded from the file system.
The modules available are:

* NetLogo
* javaWebView

All other values passed to `webview:add-module` will raise an error.

**WARNING**: Modules give the page you are on the ability to execute commands directly into NetLogo.
This allows the page the ability to run arbitrary NetLogo code, from which a malicious user could do significant damage.
This functionality should be used with great care and only on trusted pages.

Example:
```NetLogo
webview:add-module "NetLogo"     ; add the NetLogo module into the current page. See above for description of functionality
webview:add-module "javaWebView" ; adds javaWebView module into the current page.
```

The `javaWebView` module is a binding to the JavaFX WebView containing the current page.
Methods can be called on this object just like any other javascript object.
See also the [Oracle documentation for `javafx.scene.web.WebView`](https://docs.oracle.com/javafx/2/api/javafx/scene/web/WebView.html).

### `webview:null`

The null javascript value.

Example:

```NetLogo
webview:null = webview:eval "null"  ; => true
webview:null = webview:eval "2 + 2" ; => false
```

### `webview:undefined`

The undefined javascript value.

Example:
```NetLogo
webview:undefined = webview:eval "undefined" ; => true
webview:undefined = webview:eval "2 + 2"     ; => false
```

### `webview:focus`

Switch to the currently open webview tab or frame.
Does nothing if no tab or frame currently open.

Example:

```NetLogo
webview:focus
```

### `webview:is-open?`

Checks whether a Webview tab or frame is open.

Example:

```NetLogo
webview:is-open? ; => true
```

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The WebView NetLogo Extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.

