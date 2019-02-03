name := """library"""

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.3.0"
routesGenerator := InjectedRoutesGenerator
