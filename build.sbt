import sbt._


name := "akka-http-helloworld"

version := "1.0"

scalaVersion := "2.12.7"

libraryDependencies ++= {
  val akkaVersion = "2.5.17"
  val akkaHttpVersion = "10.1.5"

  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",

    "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
//    "com.typesafe.akka" %% "akka-http-experimental"  % akkaHttpVersion,
//    "com.typesafe.akka" %% "akka-http-spray-json-experimental"  % akkaHttpVersion,

    "ch.qos.logback"    %  "logback-classic" % "1.1.3",
    "org.scalatest"     %% "scalatest"       % "3.0.5"       % "test",

  )
}