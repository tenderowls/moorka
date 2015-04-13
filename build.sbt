import sbt._
import sbt.Keys._
import bintray.Keys._

val currentScalaVersion = "2.11.6"
val moorkaVersion = "0.5.0-SNAPSHOT"

scalaVersion := currentScalaVersion

val dontPublish = Seq(
  publish := { }
)

val commonSettings = Seq(
  version := moorkaVersion,
  organization := "com.tenderowls.opensource",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("http://github.com/tenderowls/moorka")),
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

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .jsSettings(utestSettingsJS:_*)
  .jvmSettings(utestSettingsJVM:_*)
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(
    normalizedName := "moorka-core",
    scalaVersion := currentScalaVersion
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val vaska = crossProject
  .crossType(CrossType.Full)
  .jsSettings(utestSettingsJS:_*)
  .jsSettings(
    //postLinkJSEnv in Test := PhantomJSEnv().value,
    libraryDependencies ++= Seq(
      "org.webjars" % "es6-shim" % "0.20.2" % "test"
    ),
    jsDependencies in Test ++= Seq(
      "org.webjars" % "es6-shim" % "0.20.2" / "es6-shim.js",
      ProvidedJS / "vaska.js"
    )
  )
  .jvmSettings(utestSettingsJVM:_*)
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(
    normalizedName := "vaska",
    scalaVersion := currentScalaVersion
  )

val vaskaJS = vaska.js
val vaskaJVM = vaska.jvm

lazy val ui = (project in file("moorka-ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(
    normalizedName := "moorka-ui",
    scalaVersion := currentScalaVersion
  )
  .dependsOn(coreJS)

lazy val root = (project in file("."))
  .settings(dontPublish:_*)
  .settings(
    scalaVersion := currentScalaVersion
  )
  .aggregate(
    ui,
    vaskaJS, vaskaJVM,
    coreJS, coreJVM
  )

