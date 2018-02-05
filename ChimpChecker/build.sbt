name := "ChimpChecker"

version := "1.0"

organization := "edu.colorado.plv.chimp.examples"

scalaVersion := "2.12.1"

unmanagedJars in Compile += file("lib/jar.jar")

resolvers += "Local Ivy2 Repository" at "file:///"+Path.userHome.absolutePath+"/.ivy2/local"


libraryDependencies += "edu.colorado.plv.fixr" %% "scalabashing" % "1.0-SNAPSHOT"

libraryDependencies += "edu.colorado.plv.chimp.combinator" %% "chimpcombinator" % "1.0-SNAPSHOT"