import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport.swaggerDomainNameSpaces
import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._
import com.typesafe.sbt.packager.universal.ZipHelper
import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys.{mappings, _}
import sbt.Resolver.{file => _, url => _}
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import spray.revolver.RevolverPlugin.autoImport.Revolver

import com.typesafe.sbt.packager.docker._

routesImport := Seq.empty

val verFile: File = file("./version.sbt")
val getVersionFromFile = IO.readLines(verFile).mkString
val readVersion = getVersionFromFile.replaceAll("version := ","").replaceAll("\"","")
version in ThisBuild := readVersion
val userName = sys.env.get("ART_USER").getOrElse("username environment variable not set")
val passWord = sys.env.get("ART_PASS").getOrElse("password environment variable not set")
publishTo in ThisBuild := Some("Artifactory Realm" at "http://artifactory-sdc.onsdigital.uk/artifactory/libs-release-local")
credentials in ThisBuild += Credentials("Artifactory Realm", "artifactory-sdc.onsdigital.uk", userName, passWord)

lazy val Versions = new {
  val elastic4s = "6.1.3"
  val scala = "2.12.4"
  val gatlingVersion = "2.3.1"
  val scapegoatVersion = "1.3.8"
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
  mainClass in assembly := Some("play.core.server.AkkaHttpServerProvider"),
  assemblyMergeStrategy in assembly := {
    // case PathList("META-INF", "io.netty.versions.properties", xs@_ *) => MergeStrategy.last
    case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first // ES shades Joda
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val serverUniversalMappings: Seq[Def.Setting[_]] = Seq(
  // The following will cause parsers/.../resources directory to be added to the list of mappings recursively
  // excluding .md and .conf files
  mappings in Universal ++= {
    val rootDir = baseDirectory.value.getParentFile

    def directoryToAdd = rootDir / "parsers/src/main/resources"

    (directoryToAdd.*** * ("*" -- ("*.md" || "*.conf"))) pair relativeTo(rootDir)
  }
)

lazy val Resolvers: Seq[MavenRepository] = Seq(
  Resolver.typesafeRepo("releases"),
  "elasticsearch-releases" at "https://maven.elasticsearch.org/releases",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Twitter Repository" at "http://maven.twttr.com",
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)


lazy val localCommonSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion in ThisBuild := Versions.scala,
  scapegoatVersion in ThisBuild := Versions.scapegoatVersion,
  dockerUpdateLatest := true,
  version in Docker := readVersion + "-SNAPSHOT",
  dockerRepository in Docker := Some("eu.gcr.io/census-ai-dev"),
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
  // TODO: Fix the following errors highlighted by scapegoat. Remove the corresponding overrides below.
  scalacOptions in Scapegoat += "-P:scapegoat:overrideLevels:TraversableHead=Warning:OptionSize=Warning:ComparingFloatingPointTypes=Warning",
  ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
  resolvers ++= Resolvers,
  coverageExcludedPackages := ".*Routes.*;.*ReverseRoutes.*;.*javascript.*"
)

val commonDeps = Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "org.scalamock" %% "scalamock" % "4.1.0" % Test,
  "com.typesafe" % "config" % "1.3.0",
  "com.github.melrief" %% "pureconfig" % "0.3.3",
  "com.lihaoyi" %% "pprint" % "0.5.3",
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
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  guice
)

val modelDeps = Seq(ws) ++ commonDeps

val clientDeps = Seq(ws) ++ commonDeps

val parsersDeps = commonDeps

val serverDeps = Seq(
  filters,
  specs2 % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.webjars" % "swagger-ui" % "3.4.4",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % Versions.gatlingVersion % "it, test",
  "io.gatling" % "gatling-test-framework" % Versions.gatlingVersion % "it, test"
) ++ commonDeps

val uiDeps = Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.typesafe.play" %% "play-test" % "2.6.10" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "com.github.tototoshi" %% "scala-csv" % "1.3.4"
) ++ commonDeps

lazy val `address-index` = project.in(file("."))
  .settings(
    publishLocal := {},
    publish := {}
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
  .settings(serverUniversalMappings: _*)
  .settings(
    libraryDependencies ++= serverDeps,
    // "-Dlogback.debug=true" can be set to show which logfile is being used.
    dockerBaseImage := "openjdk:8",
    dockerCommands += ExecCmd("CMD", "-Dlogger.file=/opt/docker/conf/logback-gcp.xml"),
    routesGenerator := InjectedRoutesGenerator,
    swaggerDomainNameSpaces := Seq("uk.gov.ons.addressIndex.model.server.response"),
    Revolver.settings ++ Seq(
      mainClass in reStart := Some("play.core.server.ProdServerStart")
    ),
    packageBin in Universal := {
      // Get the name of the package being built
      val originalFileName = (packageBin in Universal).value

      // Create a new temp file name.
      val (base, ext) = originalFileName.baseAndExt
      val newFileName = file(originalFileName.getParent) / (base + "_dist." + ext)

      // Unzip the zip archive created
      val extractedFiles = IO.unzip(originalFileName, file(originalFileName.getParent))

      // Move any files in "parsers" directory to root
      val mappings: Set[(File, String)] = extractedFiles.map(f => {
        val relativePathWithoutRootDir = f.getAbsolutePath.substring(originalFileName.getParent.length + base.length + 2)
        val relativePathWithRootDir = f.getAbsolutePath.substring(originalFileName.getParent.length + 1)

        val justFileName = f.getName

        // if path of file starts with parsers, then add to root of zip
        if (relativePathWithoutRootDir.startsWith("parsers")) {
          val (base, ext) = f.baseAndExt
          (f, justFileName)
        } else {
          (f, relativePathWithRootDir)
        }
      })

      // Set files under bin as executable just as the universalBin task does
      val binFiles = mappings.filter { case (file, path) => path.startsWith("bin/") }
      for (f <- binFiles) f._1.setExecutable(true)

      // Zip the files up and rename to the original zip name
      ZipHelper.zip(mappings, newFileName)
      IO.move(newFileName, originalFileName)

      // Create a copy of the original distribution to have a predictable name used by Jenkins jobs to
      // push application bundle to Cloud Foundry
      IO.copyFile(originalFileName, file(originalFileName.getParent) / s"${name.value}.zip")

      // Delete temporary directory
      IO.delete(file(originalFileName.getParent + "/" + originalFileName.base))

      originalFileName
    },
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "version.app"
      IO.write(file, readVersion)
      Seq(file)
    }.taskValue
  )
  .dependsOn(
    `address-index-model`
  )
  .enablePlugins(
    GatlingPlugin,
    PlayScala,
    PlayAkkaHttpServer,
    LauncherJarPlugin,
    SbtWeb,
    JavaAppPackaging,
    GitVersioning,
    SwaggerPlugin,
    DockerPlugin
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
    PlayScala,
    PlayAkkaHttpServer
  )