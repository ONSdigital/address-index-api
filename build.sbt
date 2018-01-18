import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt.Resolver.{file => _, url => _, _}
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import NativePackagerHelper._
import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport.swaggerDomainNameSpaces
import spray.revolver.RevolverPlugin.autoImport.Revolver

lazy val Versions = new {
  val elastic4s = "6.1.2"
  val scala = "2.12.4"
}

name := "address-index"
scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/ONSdigital/address-index-api"),
    connection = "https://github.com/ONSdigital/address-index-api"
  )
)

lazy val assemblySettings: Seq[Def.Setting[_]] = Seq(
  mappings in Universal ++= directory("parsers/src/main/resources"),
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
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
    "-Ywarn-dead-code", // Warn when dead code is identified
    "-Ywarn-unused" // Warn when local and private vals, vars, defs, and types are unused
  ),
  ivyScala := ivyScala.value map(_.copy(overrideScalaVersion = true)),
  resolvers ++= Seq(
    "elasticsearch-releases"     at "https://maven.elasticsearch.org/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Twitter Repository"         at "http://maven.twttr.com",
    "Artima Maven Repository"    at "http://repo.artima.com/releases",
    "scalaz-bintray"             at "https://dl.bintray.com/scalaz/releases"
  )
)

val commonDeps = Seq(
  "org.scalatest"          %% "scalatest"         % "3.0.0" % Test,
  "com.typesafe"           %  "config"            % "1.3.0",
  "com.github.melrief"     %% "pureconfig"        % "0.3.3",
  "com.lihaoyi"            %% "pprint"            % "0.5.3",
  "com.sksamuel.elastic4s" %% "elastic4s-core" % Versions.elastic4s excludeAll ExclusionRule(organization = "org.apache.logging.log4j"),
   // for the http client
  "com.sksamuel.elastic4s" %% "elastic4s-http" % Versions.elastic4s excludeAll ExclusionRule(organization = "org.apache.logging.log4j"),
  // for the tcp client
  "com.sksamuel.elastic4s" %% "elastic4s-tcp" % Versions.elastic4s excludeAll ExclusionRule(organization = "org.apache.logging.log4j"),

  // if you want to use reactive streams
 // "com.sksamuel.elastic4s" %% "elastic4s-streams" % Versions.elastic4s,
  // testing
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Versions.elastic4s % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % Versions.elastic4s % "test",
  "org.apache.logging.log4j" % "log4j-core" % "2.8.2" % "test",
  "org.apache.logging.log4j" % "log4j-api" % "2.8.2" % "test",
// old
//  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % Versions.elastic4s,
 // "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Versions.elastic4s,
  "org.apache.commons"     %  "commons-lang3"     % "3.3.2",
  guice
)

val modelDeps = Seq(ws) ++ commonDeps

val clientDeps = Seq(ws) ++ commonDeps

val parsersDeps = commonDeps

val serverDeps = Seq(
  filters,
  specs2 % Test,
  "org.scalatestplus.play"   %% "scalatestplus-play" % "3.1.2" % Test,
  "org.webjars" % "swagger-ui" % "3.4.4"
 )++ commonDeps

val uiDeps = Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.typesafe.play"      %% "play-test"          % "2.6.6" % Test ,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test ,
  "com.github.tototoshi"   %% "scala-csv"          % "1.3.4"
) ++ commonDeps

lazy val `address-index` = project.in(file("."))
  .settings(
    publishLocal := {},
    publish      := {}
  )
  .aggregate(
    `address-index-model`,
    `address-index-client`,
    `address-index-server`,
    `address-index-parsers`,
    `address-index-demo-ui`
  )

lazy val `address-index-model` = project.in(file("model"))
  .settings(localCommonSettings: _*)
  .settings(
    libraryDependencies ++= modelDeps
  ).dependsOn(
    `address-index-parsers`
  )

lazy val `address-index-client` = project.in(file("client"))
  .settings(localCommonSettings: _*)
  .settings(
    libraryDependencies ++= clientDeps
  ).dependsOn(
    `address-index-model`
  ).enablePlugins(
    SbtWeb
  )

lazy val `address-index-server` = project.in(file("server"))
  .settings(localCommonSettings: _*)
  .settings(
    libraryDependencies ++= serverDeps,
    routesGenerator := InjectedRoutesGenerator,
    swaggerDomainNameSpaces := Seq("uk.gov.ons.addressIndex.model.server.response"),
    Revolver.settings ++ Seq(
      mainClass in reStart := Some("play.core.server.ProdServerStart")
    ),
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "version.app"
      val contents = git.gitHeadCommit.value.map{ sha => s"v_$sha" }.getOrElse("develop")
      IO.write(file, contents)
      Seq(file)
    }.taskValue
  )
  .dependsOn(
    `address-index-model`
  )
  .enablePlugins(
    PlayScala,
  //  PlayNettyServer,
  //  PlayAkkaHttpServer,
    SbtWeb,
    JavaAppPackaging,
    GitVersioning,
    SwaggerPlugin
  )

lazy val `address-index-parsers` = project.in(file("parsers"))
  .settings(localCommonSettings: _*)
  .settings(libraryDependencies ++= parsersDeps)

lazy val `address-index-demo-ui` = project.in(file("demo-ui"))
  .settings(localCommonSettings: _*)
  .settings(
    libraryDependencies ++= uiDeps,
    routesGenerator := InjectedRoutesGenerator
  )
  .dependsOn(
    `address-index-client`
  )
  .enablePlugins(
    PlayScala
  //  ,
  //  PlayNettyServer,
  //  PlayAkkaHttpServer
  )