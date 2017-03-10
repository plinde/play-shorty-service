

name := "play-shorty-service"

version := "1.0"

lazy val `play-shorty-service` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs,
  "io.searchbox" % "jest" % "2.0.3",
  "org.elasticsearch" % "elasticsearch" % "2.3.5",
  "org.easytesting" % "fest-assert" % "1.4"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

// setting a maintainer which is used for all packaging types
maintainer := "Peter Linde"

// exposing the play ports
dockerExposedPorts in Docker := Seq(9000)

// run this with: docker run -p 9000:9000 play-2-3:1.0-SNAPSHOT

//http://www.scala-sbt.org/sbt-native-packager/formats/docker.html
//https://jazmit.github.io/2015/06/26/shippable-play-docker.html

//http://stackoverflow.com/questions/33408626/how-to-pass-system-property-to-docker-containers
//http://stackoverflow.com/questions/30494050/how-to-pass-environment-variables-to-docker-containers
//http://blog.michaelhamrah.com/2014/11/clustering-akka-applications-with-docker-version-3/


dockerEntrypoint in Docker := Seq(
  "sh", "-c", "bin/play-shorty-service $*"
)

//dockerEntrypoint := Seq(
//  "bin/play-shorty-service $*"

//  "-DELASTICSEARCH_PROTO=$ELASTICSEARCH_PROTO",
//  "-DELASTICSEARCH_HOST=$ELASTICSEARCH_HOST",
//  "-DELASTICSEARCH_PORT=$ELASTICSEARCH_PORT",
//  "-DELASTICSEARCH_USER=$ELASTICSEARCH_USER",
//  "-DELASTICSEARCH_PASS=$ELASTICSEARCH_PASS"

  //"-Dconfig.resource=my-application-name.conf",
  //"-Dlogger.resource=my-app-logging-conf.xml"

//)
