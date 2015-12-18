package org.nlogo.extensions.webview

import java.io.File

import java.net.URL

import javafx.embed.swing.JFXPanel

class WebViewStateManager(bridge: JavascriptBridge, browserFactory: WebViewFactory = JavaFXWebView) {
  val _ = new JFXPanel()
  var container: Option[Container]    = None
  val webView                         = browserFactory.browser(bridge)

  def close(): Unit = {
    container.foreach(_.close())
    container = None
  }

  def executeJS(s: String) = {
    if (container.isEmpty)
      throw new IllegalStateException("cannot eval js before opening webview")
    webView.executeJS(s)
  }

  def executeJSForResult(js: String): AnyRef = {
    if (container.isEmpty)
      throw new IllegalStateException("cannot eval js before opening webview")
    webView.executeJSForResult(js)
  }

  def addModule(name: String, obj: AnyRef): Unit = {
    webView.bind(name, obj, true)
  }

  def load(url: URL): Unit = webView.load(url)

  def reload(): Unit = webView.reload()

  def frame(frameContainer: Container, doc: URL) =
    openContainer(frameContainer, doc, "frame")

  def tab(tabContainer: Container, doc: URL) =
    openContainer(tabContainer, doc, "tab")

  protected def openContainer(newContainer: Container, doc: URL, containerName: String) = {
    if (container.nonEmpty)
      throw new IllegalStateException("webview window already open")
    else {
      webView.load(doc)
      newContainer.show(webView)
      container = Some(newContainer)
    }
  }
}
