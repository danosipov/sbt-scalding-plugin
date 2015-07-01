package com.danosipov.sbtplugin.scalding

import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin
import xsbt.api.Discovery


object ScaldingPlugin extends AutoPlugin {

  import AssemblyPlugin.autoImport._
  import sbt.Cache._

  override def requires: Plugins = plugins.JvmPlugin && AssemblyPlugin

  override def projectSettings: Seq[Def.Setting[_]] = {
    import ScaldingPlugin.Dependency._
    Seq(
      libraryDependencies ++= Seq(
        // scalaCompiler,
        scalding_args,
        scalding_core,
        scalding_date,
        scalding_json,
        scalding_repl,
        algebird_core,
        algebird_util,
        hadoop_core,
        scalaTest
      ),
      resolvers ++= Resolvers.allResolvers,
      assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map excludeJars,
      assemblyMergeStrategy in assembly := {
        case "project.clj" => MergeStrategy.discard // Leiningen build files
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      }
    ) ++ scaldingSettings
  }

  def excludeJars(cp: Classpath) = {
    cp foreach println
    val excludes = Set(
      "scala-compiler.jar",
      "jsp-api-2.1-6.1.14.jar",
      "jsp-2.1-6.1.14.jar",
      "jasper-compiler-5.5.12.jar",
      "minlog-1.2.jar", // Otherwise causes conflicts with Kyro (which Scalding pulls in)
      "janino-2.5.16.jar", // Janino includes a broken signature, and is not needed anyway
      "commons-beanutils-core-1.8.0.jar", // Clash with each other and with commons-collections
      "commons-beanutils-1.7.0.jar",
      "stax-api-1.0.1.jar",
      "asm-3.1.jar",
      "scalatest-2.0.jar",
      "mockito-all.jar"
    )
    cp filter { jar => excludes(jar.data.getName) }
  }

  // Copied from @deanwampler's Activator template
  lazy val run = inputKey[Unit]("Run one of the Scalding scripts.")
  //lazy val console = TaskKey[Unit]("Launch Scalding REPL")
  lazy val discoveredJobs = TaskKey[Seq[String]]("discovered-jobs", "Auto-detects Scalding jobs.")

  lazy val scaldingTask = run := {
    import sbt.complete.DefaultParsers._
    val log = streams.value.log
    val args: Vector[String] = spaceDelimited("script>").parsed.toVector
    if (args.size > 0) {
      val mainClass = "com.twitter.scalding.Tool"
      val actualArgs = Vector[String](args.head, "--local") ++ args.tail
      log.info(s"Running scala $mainClass ${actualArgs.mkString(" ")}")
      try {
        output(log,
          (runner in run).value.run(mainClass, Attributed.data((fullClasspath in assembly).value),
            actualArgs, streams.value.log))
      } catch {
        case e: Exception => println("Caught: " + e); throw e;
      }
    } else {
      // Find the available scripts and build a help message.
      log.error("Please specify one of the following commands (example arguments shown):")
      val scriptCmds = discoveredJobs.value.map(file => s"run $file [arguments] (see the source file for details)")
      scriptCmds foreach (s => log.error(s"  $s"))
      log.error("scalding requires arguments.")
    }
  }

  // TODO: maybe later
//  lazy val consoleTask = console := {
//    val log = streams.value.log
//    val mainClass = "com.twitter.scalding.ScaldingShell"
//    output(log,
//      (runner in run).value.run(mainClass, Attributed.data((fullClasspath in assembly).value),
//        List("--local"), streams.value.log))
//  }

  lazy val scaldingSettings = Seq(
    scaldingTask,
    discoveredJobs <<= (compile in Compile) map discoverJobs storeAs discoveredJobs
  )

  run <<= run.dependsOn (compile in Compile)

  def discoverJobs(analysis: inc.Analysis): Seq[String] = {
    Discovery(Set("com.twitter.scalding.Job"), Set.empty)(Tests.allDefs(analysis)).flatMap { d =>
      val (definition, discovery) = d
      if (discovery.baseClasses.contains("com.twitter.scalding.Job")) {
        Some(definition.name())
      } else None
    }
  }

  private def output(log: Logger, os: Option[String]): Unit =
    os foreach (s => log.info(s"|  $s"))

  object Dependency {
    object Version {
      val Scalding  = "0.15.0"
      val Algebird  = "0.10.1"
      val Hadoop    = "1.0.3"
      val ScalaTest = "2.2.4"
    }

    // ---- Application dependencies ----

    // Include the Scala compiler itself for reification and evaluation of expressions.
    // FIXME: See if necessary, and how to get at the Scala version
    //val scalaCompiler  = "org.scala-lang" %  "scala-compiler" % scalaVersion.

    val scalding_args     = "com.twitter"    %% "scalding-args"     % Version.Scalding
    val scalding_avro     = "com.twitter"    %% "scalding-avro"     % Version.Scalding
    val scalding_core     = "com.twitter"    %% "scalding-core"     % Version.Scalding
    val scalding_date     = "com.twitter"    %% "scalding-date"     % Version.Scalding
    val scalding_jdbc     = "com.twitter"    %% "scalding-jdbc"     % Version.Scalding
    val scalding_json     = "com.twitter"    %% "scalding-json"     % Version.Scalding
    val scalding_macros   = "com.twitter"    %% "scalding-macros"   % Version.Scalding
    val scalding_parquet  = "com.twitter"    %% "scalding-parquet"  % Version.Scalding
    val scalding_repl     = "com.twitter"    %% "scalding-repl"     % Version.Scalding
    val algebird_core     = "com.twitter"    %% "algebird-core"     % Version.Algebird
    val algebird_util     = "com.twitter"    %% "algebird-util"     % Version.Algebird

    val scalding_parquet_scrooge  = "com.twitter"    %% "scalding-parquet-scrooge"  % Version.Scalding
    val scalding_serialization_macros  = "com.twitter"    %% "scalding-serialization-macros"  % Version.Scalding

    val hadoop_core       = "org.apache.hadoop" % "hadoop-core"     % Version.Hadoop % "provided"

    val scalaTest         = "org.scalatest"    %% "scalatest"       % Version.ScalaTest %  "test"
  }

  object Resolvers {
    val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    val sonatype = "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases"
    val mvnrepository = "MVN Repo" at "http://mvnrepository.com/artifact"
    val conjars  = "Concurrent Maven Repo" at "http://conjars.org/repo"
    val clojars  = "Clojars Repo" at "http://clojars.org/repo"
    val twitterMaven = "Twitter Maven" at "http://maven.twttr.com"

    val allResolvers = Seq(typesafe, sonatype, mvnrepository, conjars, clojars, twitterMaven)
  }
}
