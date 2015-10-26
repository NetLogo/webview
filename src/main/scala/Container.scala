package org.nlogo.extensions.webview

trait Container {
  def show(browser: WebView)
  def close()
  def focus()
}

