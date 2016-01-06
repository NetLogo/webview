package org.nlogo.extensions.webview

import java.io.File
import java.net.{ URI, URL }
import java.awt.Desktop

import org.nlogo.app.App
import org.nlogo.api._
import org.nlogo.api.Syntax._
import org.nlogo.api.ScalaConversions._

import scala.util.Try

class WebViewExtension extends DefaultClassManager {
  var folder = Option.empty[File]
  var stateManager = Option.empty[WebViewStateManager]

  override def runOnce(extensionManager: ExtensionManager) = {
    folder = Some(new File(extensionManager.resolvePathAsURL("webview").replaceAll("file:", "")))
  }

  override def load(prims: PrimitiveManager) = {
    val bridge = new NetLogoJavascriptBridge
    val manager =
      new WebViewStateManager(bridge)
    prims.addPrimitive("add-module",   new AddModule(manager, bridge))
    prims.addPrimitive("browse",       Browse)
    prims.addPrimitive("close",        new Close(manager))
    prims.addPrimitive("create-frame", new CreateFrame(folder, manager))
    prims.addPrimitive("create-tab",   new CreateTab(folder, manager))
    prims.addPrimitive("exec",         new JsExec(manager))
    prims.addPrimitive("eval",         new JsEval(manager))
    prims.addPrimitive("focus",        new Focus(manager))
    prims.addPrimitive("is-open?",     new IsOpen(manager))
    prims.addPrimitive("load",         new Load(manager))
    prims.addPrimitive("reload",       new Reload(manager))
    prims.addPrimitive("null",         new JsNull)
    prims.addPrimitive("undefined",    new JsUndefined)
    prims.addPrimitive("unfocus",      Unfocus)
    stateManager = Some(manager)
  }

  override def unload(em: ExtensionManager) = {
    stateManager.foreach(_.close())
  }
}

trait DefaultOpenArgs {
  def folder: Option[File]

  def folderValue = folder.getOrElse(
    throw new ExtensionException("could not locate root directory of webview extension"))

  def titleAndSourceDoc(args: Array[Argument]): (String, URL) = {
    val title = if (args.length == 0) "Webview" else args(0).getString
    val document =
      if (args.length <= 1)
        new File(folderValue, "html/index.html").toURI.toURL
      else
        Try(new URL(args(1).getString)).toOption
          .getOrElse(new File(folderValue, s"html/${args(1).getString}").toURI.toURL)
    (title, document)
  }
}

class CreateFrame(val folder: Option[File], manager: WebViewStateManager)
  extends DefaultCommand with DefaultOpenArgs {
    val frame = new Frame()

  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array(Syntax.StringType | Syntax.RepeatableType))

  override def getAgentClassString: String = "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit =
    try {
      val (title, sourceDoc) = titleAndSourceDoc(args)
      frame.setTitle(title)
      manager.frame(frame, sourceDoc)
    } catch {
      case e: IllegalStateException =>
        throw new ExtensionException(e.getMessage)
    }
}

class CreateTab(val folder: Option[File], manager: WebViewStateManager)
  extends DefaultCommand with DefaultOpenArgs {
    val tab = new Tab

  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array(Syntax.StringType | Syntax.RepeatableType))

  override def getAgentClassString: String = "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit =
    try {
      val (title, sourceDoc) = titleAndSourceDoc(args)
      tab.setTitle(title)
      manager.tab(tab, sourceDoc)
    } catch {
      case e: IllegalStateException =>
        throw new ExtensionException(e.getMessage)
    }
}

class JsExec(manager: WebViewStateManager) extends DefaultCommand {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))

  override def getAgentClassString: String = "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit = {
    try {
      val js = args(0).getString
      manager.executeJS(js)
    } catch {
      case e: IllegalStateException => throw new ExtensionException(s"webview: ${e.getMessage}")
    }
  }
}

class JsEval(manager: WebViewStateManager) extends DefaultReporter {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(Array(Syntax.StringType), Syntax.WildcardType)

  override def getAgentClassString: String = "OTPL"

  override def report(args: Array[Argument], context: Context): AnyRef = {
    try {
      val js = args(0).getString
      manager.executeJSForResult(js)
    } catch {
      case e: IllegalStateException => throw new ExtensionException(s"webview: ${e.getMessage}")
    }
  }
}

class JsNull extends DefaultReporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(Array[Int](), Syntax.WildcardType)

  override def getAgentClassString: String = "OTPL"

  override def report(args: Array[Argument], context: Context): AnyRef = null
}

class JsUndefined extends DefaultReporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(Array[Int](), Syntax.WildcardType)

  override def getAgentClassString: String = "OTPL"

  // this is the actual value returned by JavaFX when evaluating undefined
  // -- sigh --  RG 9/24/15
  override def report(args: Array[Argument], context: Context): AnyRef =
    "undefined"
}

class Close(manager: WebViewStateManager) extends DefaultCommand {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array[Int]())

  override def getAgentClassString: String =
    "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit =
    manager.close()
}

class Load(manager: WebViewStateManager) extends DefaultCommand {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))

  override def getAgentClassString: String =
    "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit =
    try {
      manager.load(new URL(args(0).getString))
    } catch {
      case m: java.net.MalformedURLException =>
        throw new ExtensionException(s"webview: invalid URL: ${args(0).getString}")
    }
}

class Reload(manager: WebViewStateManager) extends DefaultCommand {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array[Int]())

  override def getAgentClassString: String =
    "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit =
    manager.reload()
}

class AddModule(manager: WebViewStateManager, bridge: JavascriptBridge) extends DefaultCommand {
  def modules = Map[String, Object](
    "NetLogo" -> bridge,
    "javaWebView" -> manager.webView.webView
  )

  override def getSyntax: Syntax =
    Syntax.commandSyntax(Array(Syntax.StringType))

  override def getAgentClassString: String =
    "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit = {
    val moduleName = args(0).getString
    val module = modules.getOrElse(moduleName, throw new ExtensionException("Invalid module: " + moduleName))
    manager.addModule(moduleName, module)
  }
}

class IsOpen(manager: WebViewStateManager) extends DefaultReporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(Array[Int](), Syntax.BooleanType)

  override def getAgentClassString: String = "OTPL"

  override def report(args: Array[Argument], context: Context): AnyRef =
    Boolean.box(manager.container.nonEmpty)
}

class Focus(manager: WebViewStateManager) extends DefaultCommand {
  override def getSyntax: Syntax = Syntax.commandSyntax(Array[Int]())

  override def getAgentClassString: String = "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit = {
    manager.container.foreach(_.focus())
  }
}

object Unfocus extends DefaultCommand {
  override def getSyntax: Syntax = Syntax.commandSyntax(Array[Int]())

  override def getAgentClassString: String = "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit = {
    Container.unfocus()
  }
}

object Browse extends DefaultCommand {
  override def getSyntax: Syntax = Syntax.commandSyntax(Array[Int](Syntax.StringType))

  override def getAgentClassString: String = "OTPL"

  override def perform(args: Array[Argument], context: Context): Unit = {
    if (Desktop.isDesktopSupported)
      Desktop.getDesktop.browse(new URI(args(0).getString))
  }
}
