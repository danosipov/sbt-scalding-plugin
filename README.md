An autoplugin for SBT, enabling Scalding projects. Brings in necessary dependencies, configures assembly settings, enables local execution.

Configuration
=============

* Create new SBT project
* Add the following lines to project/plugins.sbt
```
resolvers += Resolver.url("bintray-danosipov-sbt-plugin-releases",
  url("http://dl.bintray.com/content/danosipov/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.danosipov" % "sbt-scalding-plugin" % "1.0.0")
```
* Add the following to /build.sbt
```
enablePlugins(ScaldingPlugin)
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
