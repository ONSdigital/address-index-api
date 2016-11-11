import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt._
import sbt.Resolver.{file => _, url => _, _}

lazy val Versions = new {
  val elastic4s = "2.4.0"
  val scala = "2.11.8"
}

name := "address-index"
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/ONSdigital/address-index-api"),
    connection = "https://github.com/ONSdigital/address-index-api"
  )
)

lazy val assemblySettings: Seq[Def.Setting[_]] = Seq(
  assemblyJarName in assembly := "ons-ai-api.jar",
  mainClass in assembly := Some("play.core.server.NettyServer"),
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "io.netty.versions.properties", xs@_ *) => MergeStrategy.last
    case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first // ES shades Joda
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val localCommonSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion in ThisBuild := Versions.scala,
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
  ),
  ivyScala := ivyScala.value map(_.copy(overrideScalaVersion = true)),
  resolvers ++= Seq(
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Twitter Repository"         at "http://maven.twttr.com",
    "Artima Maven Repository"    at "http://repo.artima.com/releases"
  )
)

val commonDeps = Seq(
  "org.scalatest"          %% "scalatest"         % "3.0.0" % Test,
  "com.typesafe"           %  "config"            % "1.3.0",
  "com.github.melrief"     %% "pureconfig"        % "0.3.1.1",
  "com.lihaoyi"            %% "pprint"            % "0.4.3",
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % Versions.elastic4s,
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Versions.elastic4s
)

val modelDeps = Seq(ws) ++ commonDeps

val clientDeps = Seq(ws)++ commonDeps

val parsersDeps = Seq(
  "com.github.vinhkhuc" % "jcrfsuite" % "0.6"
) ++ commonDeps

val serverDeps = Seq.empty ++ commonDeps

val uiDeps = Seq(
  jdbc,
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap"     % "3.3.7",
  "org.webjars" % "jquery"        % "3.1.1",
  "org.mockito" % "mockito-all"   % "1.10.19" % Test,
  "org.webjars" % "font-awesome"  % "4.6.3"
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
  .settings(localCommonSettings: _*).settings(
  libraryDependencies ++= modelDeps
)

lazy val `address-index-client` = project.in(file("client"))
  .settings(localCommonSettings: _*).settings(
  libraryDependencies ++= clientDeps
).dependsOn(
  `address-index-model`
).enablePlugins(
  PlayScala,
  SbtWeb
)

lazy val `address-index-server` = project.in(file("server"))
  .settings(localCommonSettings: _*).settings(
  libraryDependencies ++= serverDeps,
  routesGenerator := InjectedRoutesGenerator
).dependsOn(
  `address-index-model`,
  `address-index-parsers`
).enablePlugins(
  PlayScala,
  SbtWeb,
  JavaAppPackaging
)

lazy val `address-index-parsers` = project.in(file("parsers"))
  .settings(localCommonSettings: _*).settings(
  libraryDependencies ++= parsersDeps
)

lazy val `address-index-demoui` = project.in(file("demoui"))
  .settings(localCommonSettings: _*).settings(
  libraryDependencies ++= uiDeps,
  routesGenerator := InjectedRoutesGenerator
).dependsOn(
  `address-index-client`
).enablePlugins(
  PlayScala,
  SbtWeb
)