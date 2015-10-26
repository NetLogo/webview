package org.nlogo.extensions.webview

import java.awt.BorderLayout
import java.awt.Component

import javax.swing.{ JPanel, SwingUtilities }

import javafx.application.Platform
import javafx.scene.{ Group, Scene }
import javafx.scene.shape.Rectangle
import javafx.scene.paint.Paint
import javafx.embed.swing.JFXPanel

import org.nlogo.app.App
import org.nlogo.extensions.webview.util.FunctionToCallback.function2Runnable
import org.nlogo.window.ThreadUtils

import scala.collection.JavaConversions._

class Tab extends JPanel with Container {
  val panel = new JFXPanel()
  Platform.setImplicitExit(false)
  val group = new Group()
  add(panel, BorderLayout.PAGE_END)

  var scene = Option.empty[Scene]

  @volatile var tabIndex: Int = 0

  Platform.runLater { () =>
    scene = Some(new Scene(group))
    panel.setScene(scene.get)
  }

  def show(browser: WebView) = {
    Platform.runLater { () =>
      group.getChildren.add(browser.webView)
      val (w, h) = (group.getLayoutBounds.getWidth, group.getLayoutBounds.getHeight)
      SwingUtilities.invokeLater { () =>
        panel.setPreferredSize(new java.awt.Dimension(w.toInt, h.toInt))
        tabIndex = App.app.tabs.getTabCount
        App.app.tabs.insertTab(getName, null, this, "JavaFX Tab", tabIndex)
      }
    }
  }

  def setTitle(title: String) =
    SwingUtilities.invokeLater { () =>
      setName(title)
      if (tabIndex != 0)
        App.app.tabs.setTitleAt(tabIndex, title)
    }

  def close() = {
    Platform.runLater { () =>
      group.getChildren.clear()
    }
    SwingUtilities.invokeLater { () =>
      App.app.tabs.remove(this)
      tabIndex = 0
    }
  }

  override def focus() = {
    SwingUtilities.invokeLater { () =>
      App.app.tabs.setSelectedIndex(tabIndex)
    }
  }
}
