import sbt._
import sbt.Keys._
import scala.scalajs.ir.ScalaJSVersions
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import bintray.Keys._

val dontPublish = Seq(
  publish := { }
)

val SCALA_VERSION = "2.11.2"

scalaVersion := SCALA_VERSION

val commonSettings = Seq(
  scalaVersion := SCALA_VERSION,
  version := "0.0.1",
  organization := "com.tenderowls.opensource",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("http://github.com/tenderowls/moorka"))
)

val publishSettings = bintraySettings ++ bintrayPublishSettings ++ Seq(
  repository in bintray := "moorka",
  bintrayOrganization in bintray := Some("tenderowls"),
  publishMavenStyle := false
)

lazy val `moorka-core` = (project in file("moorka-core"))
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(scalaJSSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
      "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % ScalaJSVersions.current % "test",
      "org.scala-lang" % "scala-reflect" % SCALA_VERSION
    )
  )

lazy val `moorka-ui` = (project in file("moorka-ui"))
  .settings(publishSettings:_*)
  .settings(commonSettings:_*)
  .settings(scalaJSSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
    )
  )
  .dependsOn(`moorka-core`)

lazy val `moorka-todomvc` = (project in file("moorka-todomvc"))
  .settings(dontPublish:_*)
  .settings(commonSettings:_*)
  .settings(scalaJSSettings: _*)
  .dependsOn(`moorka-ui`)

lazy val root = (project in file("."))
  .settings(dontPublish:_*)
  .settings(
    scalaVersion := SCALA_VERSION
  )
  .aggregate(
    `moorka-core`,
    `moorka-ui`,
    `moorka-todomvc`
  )

