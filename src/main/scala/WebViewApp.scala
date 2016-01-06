package org.nlogo.extensions.webview

import java.io.File
import javax.swing.WindowConstants

object WebViewApp extends App {
  val stateManager = new WebViewStateManager(new DummyJavascriptBridge())
  val frame = new Frame("Webview", WindowConstants.EXIT_ON_CLOSE) {
    override def onXClicked(): Unit = {
      stateManager.close()
    }
  }
  stateManager.frame(frame, new File("html/index.html").toURI.toURL)

  val addHelloMessage =
    """|var newP = document.createElement('p');
       |newP.appendChild(document.createTextNode('hello!'));
       |document.body.appendChild(newP);""".stripMargin
  stateManager.executeJS(addHelloMessage)
}
