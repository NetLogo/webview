package org.nlogo.extensions.webview

import java.net.URL
import javafx.scene.web.{ WebView => JFXWebView }

trait WebView {
  def executeJS(js: String)
  def executeJSForResult(js: String): AnyRef
  def bind(name: String, value: AnyRef, allowRemoteAccess: Boolean): Unit
  def load(path: URL): Unit
  def reload(): Unit
  def webView: JFXWebView
}

trait WebViewFactory {
  def browser(bridge: JavascriptBridge): WebView
}
