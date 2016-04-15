import sbt.Package.ManifestAttributes

scalaVersion := "2.11.7"

scalaSource in Compile := { baseDirectory.value  / "src" / "main" }

scalaSource in Test := { baseDirectory.value  / "src" / "test" }

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                        "-feature", "-encoding", "us-ascii")

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-encoding", "us-ascii")

name := "WebViewExtension"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.easymock" % "easymock" % "3.4" % "test"
)

enablePlugins(org.nlogo.build.NetLogoExtension)

netLogoVersion      := "6.0.0-M4"

netLogoClassManager := "org.nlogo.extensions.webview.WebViewExtension"

netLogoExtName      := "webview"

netLogoZipSources   := false

netLogoPackageExtras ++= (baseDirectory.value / "html" ***).get.map(
  f => (f -> ("html/" + f.getName)))

fork in run := true
