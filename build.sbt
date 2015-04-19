name := "sbt-scalding-plugin"

organization := "com.danosipov"

sbtPlugin := true

scalaVersion := "2.10.5"

version := "1.0.0"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

import bintray.Keys._

lazy val commonSettings = Seq(
  version in ThisBuild := version.value,
  organization in ThisBuild := organization.value
)

lazy val root = (project in file(".")).
  settings(commonSettings ++ bintrayPublishSettings: _*).
  settings(
    sbtPlugin := true,
    name := name.value,
    description := "TODO",
    // This is an example.  bintray-sbt requires licenses to be specified 
    // (using a canonical name).
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    publishMavenStyle := false,
    repository in bintray := "sbt-plugins",
    bintrayOrganization in bintray := None
  )