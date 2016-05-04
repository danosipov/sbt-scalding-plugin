An autoplugin for [SBT](http://www.scala-sbt.org/), enabling [Scalding](https://github.com/twitter/scalding) projects. Brings in necessary dependencies, configures assembly settings, enables local execution.

Configuration
=============

* Create new SBT project
* Add the following lines to project/plugins.sbt
```
resolvers += Resolver.url("bintray-danosipov-sbt-plugin-releases",
  url("http://dl.bintray.com/content/danosipov/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.danosipov" % "sbt-scalding-plugin" % "1.0.5")
```
* Add the following to /build.sbt
```
enablePlugins(ScaldingPlugin)
```

* Make sure the project is configured for the latest version of sbt. in project/build.properties add:
```
sbt.version=0.13.8
```

Features
========

* `sbt assembly` to build a fat jar for deployment to hadoop clusters
* `sbt run` to execute a Scalding job in local mode

Inspiration
===========

i.e. Copy & Paste from:
* https://github.com/allenai/sbt-plugins/
* https://github.com/deanwampler/activator-scalding
