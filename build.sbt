import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt.Resolver.{file => _, url => _, _}
import sbt._

name         := "address-index"
scalaVersion := "2.11.7"
scmInfo      := Some(ScmInfo(url("https://bitbucket.org/rhys_bradbury/address-index"), "scm:git:git@bitbucket.org:rhys_bradbury/address-index.git"))

lazy val localCommonSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val Versions = new {
  val play      = "2.4.2"
  val scalatest = "3.0.0"
}

val customResolvers = Seq(
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Twitter Repository"         at "http://maven.twttr.com"
)

val modelDeps = Seq.empty

val clientDeps = Seq.empty

val serverDeps = Seq(
  "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
)

val utilDeps = Seq.empty

lazy val `address-index` = project.in(file("."))
  .settings(
    publishLocal := {},
    publish      := {}
  ).aggregate(
  `address-index-model`,
  `address-index-client`,
  `address-index-server`,
  `address-index-utils`
)

lazy val `address-index-model` = project.in(file("model"))
  .settings(localCommonSettings)
  .settings(libraryDependencies ++= modelDeps)

lazy val `address-index-client` = project.in(file("client"))
  .settings(libraryDependencies ++= clientDeps)
  .settings(localCommonSettings)
  .dependsOn(`address-index-model`)

lazy val `address-index-server` = project.in(file("server"))
  .settings(
    libraryDependencies ++= serverDeps,
    resolvers           ++= customResolvers,
    routesGenerator      := InjectedRoutesGenerator
  )
  .settings(localCommonSettings)
  .dependsOn(`address-index-model`)
  .enablePlugins(PlayScala, SbtWeb, JavaAppPackaging)

lazy val `address-index-utils` = project.in(file("utils"))
  .settings(localCommonSettings)
  .settings(libraryDependencies ++= modelDeps)