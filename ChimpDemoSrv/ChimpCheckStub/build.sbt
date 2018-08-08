name := "ChimpTrainerStub"

version := "1.0"

organization := "edu.colorado.plv"

scalaVersion := "2.12.1"

mainClass in (Compile, run) := Some("edu.colorado.plv.chimp.stub.StubGenerator")

resolvers += "Local Ivy2 Repository" at "file:///"+Path.userHome.absolutePath+"/.ivy2/local"

libraryDependencies += "edu.colorado.plv.fixr" %% "scalabashing" % "1.0-SNAPSHOT"
libraryDependencies += "edu.colorado.plv.chimp.combinator" %% "chimpcombinator" % "1.0-SNAPSHOT"
