[![Build Status](https://travis-ci.org/BorisNaguet/sbt-cxf-wsdl2java.svg?branch=master)](https://travis-ci.org/BorisNaguet/sbt-cxf-wsdl2java)
[ ![Download](https://api.bintray.com/packages/borisnaguet/ivy/sbt-cxf-wsdl2java/images/download.svg) ](https://bintray.com/borisnaguet/ivy/sbt-cxf-wsdl2java/_latestVersion)

sbt-cxf-wsdl2java
=================

An sbt plugin for generating java artifacts from WSDL using cxf.

For the moment, this fork only updates scala (to 2.10.6 because it's the version used to RUN sbt but your project can be 2.11 or 2.12), sbt (to 0.13.15) and CXF (to 3.1.12).

## Requirements

* [SBT 0.13.5+](http://www.scala-sbt.org/)
* Compatible with SBT 1.x (since version 0.2.7)

## Quick start

Add plugin to *project/plugins.sbt*:

```scala
resolvers ++= Seq(Resolver.bintrayIvyRepo("borisnaguet", "ivy"))
addSbtPlugin("io.github.borisnaguet" % "sbt-cxf-wsdl2java" % "0.2.7")
```

## Configuration

Plugin keys are prefixed with "cxf".

* **wsdl2javaDefaultArgs**: override the default arguments passed to wsdl2java, supply another Seq[String] of arguments.
* **cxfParallelExecution**: set to *false* to disable running wsdl2java commands in parallel. Useful if there are duplicate classes to be generated and the output directory for multiple services are the same
* **cxfVersion**: override the version of cxf to be used
* **cxfJaxb2BasicsVersion**: override the version of jaxb2_commons to be used

### Add Wsdls

```scala
lazy val wsclientPackage := "io.github.borisnaguet.sample"

cxfWsdls := Seq(
  CxfWsdl((resourceDirectory in Compile).value / "Sample.wsdl", Seq("-p",  wsclientPackage), "unique wsdl id"),
  // add -wsdlLocation for the wsdls to be loaded by the classloader (portable solution)
  CxfWsdl(
    (resourceDirectory in Compile).value / "wsdls/other.wsdl",
    Seq("-p",  "com.company.project", "-wsdlLocation", "wsdls/other.wsdl"),
    "other wsdl id"
  ),
  ...
)

cxfWsdlsUrls := Seq(
  CxfWsdlUrl(
    url("http://www.my-weather-channel.org/global-weather?wsdl"),
    Seq("-p", "global-weather"),
    "org.my-weather-channel.global-weather"
  )
)
```

## Commands

```~wsdl2java``` To automatically generate source code when a wsdl is changed
