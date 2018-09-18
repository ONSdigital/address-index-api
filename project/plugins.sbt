logLevel := Level.Warn

addSbtPlugin("com.typesafe.play"      %  "sbt-plugin"            % "2.6.10" exclude("org.slf4j", "slf4j-simple"))
addSbtPlugin("net.virtual-void"       %  "sbt-dependency-graph"  % "0.8.2")
addSbtPlugin("com.eed3si9n"           %  "sbt-assembly"          % "0.14.3")
addSbtPlugin("com.typesafe.sbt"       %  "sbt-native-packager"   % "1.3.3")
addSbtPlugin("ch.jodersky"            %  "sbt-jni"               % "1.2.4")
addSbtPlugin("io.spray"               %  "sbt-revolver"          % "0.8.0")
addSbtPlugin("com.typesafe.sbt"       %  "sbt-git"               % "0.9.0")
addSbtPlugin("com.iheart"             %  "sbt-play-swagger"      % "0.6.2-PLAY2.6")
addSbtPlugin("io.gatling"             %  "gatling-sbt"           % "2.2.2")
addSbtPlugin("org.scalastyle"         %  "scalastyle-sbt-plugin" % "0.8.0")
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat"         % "1.0.5")
addSbtPlugin("org.scoverage"          %  "sbt-scoverage"         % "1.5.0")
