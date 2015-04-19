// build root project
lazy val root = Project("plugins", file(".")) dependsOn(scaldingPlugin)

// depends on the Scalding Plugin project
lazy val scaldingPlugin = file("..").getAbsoluteFile.toURI