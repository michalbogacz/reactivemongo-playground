name := "reactivemongo-playground"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "1.0.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)