lazy val commonSettings = Seq(
  name := "ChimpCombinator",
  organization := "edu.colorado.plv.chimp.combinator",
  version := "1.0",
  scalaVersion := "2.12.1"
  // scalacOptions += "-target:jvm-1.7"
)

name := "ChimpCombinator"

organization := "edu.colorado.plv.chimp.combinator"

version := "1.0"

scalaVersion := "2.12.1"

// scalacOptions += "-target:jvm-1.7"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"

resolvers += "Local Ivy2 Repository" at "file:///"+Path.userHome.absolutePath+"/.ivy2/local"

PB.targets in Compile := Seq(
  scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
)

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4"

libraryDependencies += "edu.colorado.plv.fixr" %% "scalabashing" % "1.0-SNAPSHOT"
