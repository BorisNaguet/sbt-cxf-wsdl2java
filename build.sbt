sbtPlugin := true

name := "sbt-cxf-wsdl2java"

organization := "io.github.borisnaguet"

version := "0.2.0"

scalaVersion := "2.10.6"

publishMavenStyle := false

//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (version.value.trim.endsWith("SNAPSHOT"))
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

bintrayRepository := "ivy"
bintrayPackageLabels := Seq("sbt", "plugin", "cxf")

pomExtra := (
  <url>https://github.com/BorisNaguet/sbt-cxf-wsdl2java</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:BorisNaguet/sbt-cxf-wsdl2java.git</url>
    <connection>scm:git:git@github.com:BorisNaguet/sbt-cxf-wsdl2java.git</connection>
  </scm>
  <developers>
    <developer>
      <id>smanciot</id>
      <name>Stéphane Manciot</name>
      <url>http://www.linkedin.com/in/smanciot</url>
    </developer>
    <developer>
      <id>BorisNaguet</id>
      <name>Boris Naguet</name>
      <url>https://github.com/BorisNaguet</url>
    </developer>
  </developers>)
