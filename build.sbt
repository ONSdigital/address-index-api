import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt.Resolver.{file => _, url => _, _}
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

val scalaV = "2.11.8"
  name := "address-index"
  scalaVersion := scalaV
  scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/ONSdigital/address-index-api"),
    connection = "https://github.com/ONSdigital/address-index-api"
  )
)

lazy val assemblySettings: Seq[Def.Setting[_]] = Seq(
  assemblyJarName in assembly := "ons-bi-api.jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "io.netty.versions.properties", xs@_ *) => MergeStrategy.last
    case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first // ES shades Joda
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  mainClass in assembly := Some("play.core.server.NettyServer"),
  assemblyJarName in assembly := "ons-ai-api.jar"
)

lazy val localCommonSettings = Seq(
  scalaVersion := scalaV
) ++ assemblySettings

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  //"-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
  "-Ywarn-dead-code", // Warn when dead code is identified
  "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are unused
  "-Ywarn-unused-import", //  Warn when imports are unused (don't want IntelliJ to do it automatically)
  "-Ywarn-numeric-widen" // Warn when numerics are widened
)

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

val customResolvers = Seq(
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Twitter Repository"         at "http://maven.twttr.com",
  "Artima Maven Repository"    at "http://repo.artima.com/releases"
)

val commonDeps = Seq(
  "org.scalatest"          %% "scalatest"         % "3.0.0" % Test,
  "com.typesafe"           %  "config"            % "1.3.0",
  "com.github.melrief"     %% "pureconfig"        % "0.3.1.1",
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % "2.3.1"
)

val modelDeps = commonDeps
val clientDeps = commonDeps
val parsersDeps = commonDeps
val serverDeps = commonDeps
val uiDeps = Seq(
  jdbc,
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.7",
  "org.webjars" % "jquery" % "3.1.1",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.webjars" % "font-awesome" % "4.6.3"
) ++ commonDeps


lazy val `address-index` = project.in(file("."))
  .settings(
    publishLocal := {},
    publish      := {}
  ).aggregate(
  `address-index-model`,
  `address-index-client`,
  `address-index-server`,
  `address-index-parsers`,
  `address-index-demoui`
)

lazy val `address-index-model` = project.in(file("model"))
  .settings(
    localCommonSettings,
    libraryDependencies ++= modelDeps,
    resolvers           ++= customResolvers
  )

lazy val `address-index-client` = project.in(file("client"))
  .settings(
    localCommonSettings,
    libraryDependencies ++= clientDeps
  )
  .dependsOn(`address-index-server`)

lazy val `address-index-server` = project.in(file("server"))
  .settings(
    localCommonSettings,
    libraryDependencies ++= serverDeps,
    resolvers           ++= customResolvers,
    routesGenerator      := InjectedRoutesGenerator
  )
  .dependsOn(
    `address-index-model`,
    `address-index-parsers`
  )
  .enablePlugins(PlayScala, SbtWeb, JavaAppPackaging)

lazy val `address-index-parsers` = project.in(file("parsers"))
  .settings(
    localCommonSettings,
    libraryDependencies ++= parsersDeps
  )

lazy val `address-index-demoui` = project.in(file("demoui"))
  .settings(
    localCommonSettings,
    libraryDependencies ++= uiDeps,
    resolvers           ++= customResolvers,
    routesGenerator      := InjectedRoutesGenerator
  )
  .dependsOn(
    `address-index-model`,
    `address-index-parsers`
  )
  .enablePlugins(PlayScala, SbtWeb)
