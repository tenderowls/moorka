import sbt._
import sbt.Keys._
import bintray.Keys._

val currentScalaVersion = "2.11.7"
val moorkaVersion = "0.7.0"

scalaVersion := currentScalaVersion

val dontPublish = Seq(
  publish := { }
)

val commonSettings = Seq(
  version := moorkaVersion,
  organization := "org.reactivekittens",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("http://github.com/tenderowls/moorka")),
  scalaVersion := currentScalaVersion,
  scalacOptions ++= Seq("-deprecation", "-feature")
)

val utestSetting = Seq(
  testFrameworks += new TestFramework("utest.runner.Framework")
)

val utestSettingsJS = utestSetting :+ (libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.1" % "test")
val utestSettingsJVM = utestSetting :+ (libraryDependencies += "com.lihaoyi" %% "utest" % "0.3.1" % "test")

val publishSettings = moorkaVersion.endsWith("SNAPSHOT") match {
  case true => Seq(
    publishTo := Some("Flexis Thirdparty Snapshots" at "https://nexus.flexis.ru/content/repositories/thirdparty-snapshots"),
    credentials += {
      val ivyHome = sys.props.get("sbt.ivy.home") match {
        case Some(path) ⇒ file(path)
        case None ⇒ Path.userHome / ".ivy2"
      }
      Credentials(ivyHome / ".credentials")
    }
  )
  case false => bintraySettings ++ bintrayPublishSettings ++ Seq(
    repository in bintray := "moorka",
    bintrayOrganization in bintray := Some("tenderowls"),
    publishMavenStyle := false
  )
}

lazy val moorka = crossProject
  .crossType(CrossType.Pure)
  .jsSettings(utestSettingsJS:_*)
  .jvmSettings(utestSettingsJVM:_*)
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(
    normalizedName := "moorka",
    scalaVersion := currentScalaVersion
  )

lazy val moorkaJS = moorka.js
lazy val moorkaJVM = moorka.jvm

lazy val vaska = crossProject
  .crossType(CrossType.Full)
  .settings(
    unmanagedResourceDirectories in Compile += file("vaska") / "shared" / "src" / "main" / "resources"
  )
  .jsSettings(utestSettingsJS:_*)
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.webjars" % "es6-shim" % "0.20.2" % "test"
    ),
    jsDependencies in Test ++= Seq(
      "org.webjars" % "es6-shim" % "0.20.2" / "es6-shim.js",
      ProvidedJS / "localStorage.js",
      ProvidedJS / "vaska.js" dependsOn "localStorage.js"
    ),
    jsDependencies += ProvidedJS / "vaska.js"
  )
  .jvmSettings(utestSettingsJVM:_*)
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(normalizedName := "vaska")

val vaskaJS = vaska.js
val vaskaJVM = vaska.jvm

lazy val felix = crossProject
  .crossType(CrossType.Full)
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(
    normalizedName := "felix",
    unmanagedResourceDirectories in Compile += file("felix") / "shared" / "src" / "main" / "resources"
  )
  .jsSettings(utestSettingsJS:_*)
  .jsConfigure(_.dependsOn(vaskaJS  % "compile->compile;test->test", moorkaJS))
  .jsSettings(
    jsDependencies += ProvidedJS / "felix.js"
  )
  .jvmSettings(utestSettingsJVM:_*)
  .jvmSettings(
    libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.3.0"
  )
  .jvmConfigure(_.dependsOn(vaskaJVM, moorkaJVM))

val felixJS = felix.js
val felixJVM = felix.jvm

//lazy val ui = (project in file("moorka-ui"))
//  .enablePlugins(ScalaJSPlugin)
//  .settings(publishSettings:_*)
//  .settings(commonSettings:_*)
//  .settings(
//    normalizedName := "moorka-ui",
//    scalaVersion := currentScalaVersion
//  )
//  .dependsOn(coreJS)
//  .dependsOn(vaskaJS)

lazy val root = (project in file("."))
  .settings(dontPublish:_*)
  .settings(
    scalaVersion := currentScalaVersion
  )
  .aggregate(
    felixJS, felixJVM,
    vaskaJS, vaskaJVM,
    moorkaJS, moorkaJVM
  )

