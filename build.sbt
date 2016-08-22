name := "play-shorty-service"

version := "1.0"

lazy val `play-shorty-service` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs,
  "io.searchbox" % "jest" % "2.0.3",
  "org.elasticsearch" % "elasticsearch" % "2.3.5"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  