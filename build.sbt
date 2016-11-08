import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt.Resolver.{file => _, url => _, _}
import sbt._

logLevel := Level.Debug

val scalaV    = "2.11.7"
name         := "address-index"
scalaVersion := scalaV
scmInfo      := Some(ScmInfo(url("https://bitbucket.org/rhys_bradbury/address-index"), "scm:git:git@bitbucket.org:rhys_bradbury/address-index.git"))

lazy val localCommonSettings = Seq(
  scalaVersion := scalaV
)

val customResolvers = Seq(
  "ElasticSearch" at "http://maven.elasticsearch.org/releases/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Twitter Repository"         at "http://maven.twttr.com",
  "Artima Maven Repository"    at "http://repo.artima.com/releases"
)

val commonDeps = Seq(
  "org.scalatest"      %% "scalatest"  % "3.0.0" % Test,
  "com.typesafe"       %  "config"     % "1.3.0",
  "com.github.melrief" %% "pureconfig" % "0.3.1.1"
)

val modelDeps   = Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % "2.4.0"
) ++ commonDeps

val clientDeps  = commonDeps
val parsersDeps = commonDeps

val serverDeps  = Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % "2.4.0",
  "org.elasticsearch.plugin" % "shield" % "2.4.0"

) ++ commonDeps

lazy val `address-index` = project.in(file("."))
  .settings(
    publishLocal := {},
    publish      := {}
  ).aggregate(
    `address-index-model`,
    `address-index-client`,
    `address-index-server`,
    `address-index-parsers`
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