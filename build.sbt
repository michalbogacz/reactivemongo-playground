name := "reactivemongo-playground"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "1.0.3",
  "com.beachape" %% "enumeratum" % "1.6.1",
  "org.scalatest" %% "scalatest" % "3.2.3" % Test,
)