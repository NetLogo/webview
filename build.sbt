import sbt.Package.ManifestAttributes

scalaVersion := "2.11.7"

scalaSource in Compile := { baseDirectory.value  / "src" / "main" }

scalaSource in Test := { baseDirectory.value  / "src" / "test" }

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                        "-feature", "-encoding", "us-ascii")

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-encoding", "us-ascii")

name := "WebViewExtension"

libraryDependencies +=
  "org.nlogo" % "NetLogo" % "5.3" from "http://ccl.northwestern.edu/devel/NetLogo-5.3-LevelSpace-3a6b9b4.jar"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.easymock" % "easymock" % "3.4" % "test"
)

enablePlugins(org.nlogo.build.NetLogoExtension)

netLogoClassManager := "org.nlogo.extensions.webview.WebViewExtension"

netLogoExtName      := "webview"

netLogoZipSources   := false

netLogoZipExtras ++= (baseDirectory.value / "html" ***).get.map(
  f => (f -> ("html/" + f.getName)))

fork in run := true
