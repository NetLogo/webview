package org.nlogo.extensions.webview

import java.awt.Dimension
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JFrame, WindowConstants, SwingUtilities }

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.geometry.Bounds
import javafx.scene.{ Group, Scene }

import util.FunctionToCallback.{ function2ChangeListener, function2Runnable }

class Frame(
  title           : String     = "Webview",
  onClose         : Int        = WindowConstants.HIDE_ON_CLOSE)
  extends JFrame(title) with Container {

  def onXClicked(): Unit = {}

  var group = Option.empty[Group]
  val panel = new JFXPanel()
  add(panel)
  setSize(800, 600)
  setDefaultCloseOperation(onClose)
  setAutoRequestFocus(false)

  val resizeListener =
    function2ChangeListener { (oldBounds: Bounds, newBounds: Bounds) =>
      SwingUtilities.invokeLater { () =>
        val newDim = new Dimension(newBounds.getWidth.toInt, newBounds.getHeight.toInt)
        panel.setPreferredSize(newDim)
        pack()
      }
    }

  val listener = new WindowAdapter() {
    override def windowClosing(windowEvent: WindowEvent): Unit = {
      onXClicked()
    }
  }

  override def show(browser: WebView) = {
    Platform.runLater { () =>
      val webGroup = new Group(browser.webView)
      val scene = new Scene(webGroup)
      browser.webView.boundsInLocalProperty.addListener(resizeListener)
      panel.setScene(scene)
      group = Some(webGroup)
    }
    SwingUtilities.invokeLater { () =>
      addWindowListener(listener)
      setVisible(true)
    }
  }

  override def close() = {
    Platform.runLater { () =>
      panel.setScene(null)
      group.foreach(_.getChildren.clear())
      group = None
    }
    SwingUtilities.invokeLater { () =>
      removeWindowListener(listener)
      setVisible(false)
      removeAll()
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))
    }
  }

  override def focus() = {
    SwingUtilities.invokeLater { () =>
      toFront()
      requestFocus()
    }
  }
}
