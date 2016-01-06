package org.nlogo.extensions.webview


trait Container {
  def show(browser: WebView): Unit
  def close(): Unit
  def focus(): Unit
}

object Container {
  import org.nlogo.app.App
  import javax.swing.SwingUtilities
  import org.nlogo.extensions.webview.util.FunctionToCallback.function2Runnable

  def unfocus(): Unit = {
    SwingUtilities.invokeLater { () =>
      App.app.frame.toFront()
      App.app.frame.requestFocus()
      App.app.tabs.setSelectedIndex(0)
    }
  }
}
