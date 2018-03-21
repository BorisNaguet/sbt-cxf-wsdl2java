package io.github.borisnaguet.sbt.plugins

import java.io.File

import sbt.Keys._
import sbt._

/**
  * @author stephane.manciot@ebiznext.com
  *
  */
object CxfWsdl2JavaPlugin extends AutoPlugin {

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = allRequirements

  object autoImport {
    lazy val CxfConfig = config("cxf").hide

    lazy val cxfVersion = settingKey[String]("cxf version")
    lazy val cxfXjcTsVersion = settingKey[String]("cxf XJC ToString Plugin version")
    lazy val cxfJaxb2BasicsVersion = settingKey[String]("org.jvnet.jaxb2_commons:jaxb2-basics version")
    lazy val cxfLogbackVersion = settingKey[String]("org.jvnet.jaxb2_commons:jaxb2-basics version")
    lazy val cxfFilePath = settingKey[File]("bas path of generated sources")
    lazy val wsdl2java = taskKey[Seq[File]]("Generates java files from wsdls")
    lazy val cxfWsdls = settingKey[Seq[CxfWsdl]]("wsdls file paths to generate java files from")
    lazy val cxfWsdlsUrls = settingKey[Seq[CxfWsdlUrl]]("wsdls URLs to generate java files from")
    lazy val wsdl2javaDefaultArgs = settingKey[Seq[String]]("wsdl2java default arguments")
    lazy val cxfParallelExecution = settingKey[Boolean]("execute wsdl2java commands in parallel")

    lazy val cxfHttpProxyHost = settingKey[String]("set the http proxy host for the wsdl2java command (default from SBT_OPTS)")
    lazy val cxfHttpProxyPort = settingKey[Integer]("set the http proxy port for the wsdl2java command (default from SBT_OPTS)")
    lazy val cxfHttpsProxyHost = settingKey[String]("set the https proxy host for the wsdl2java command (default from SBT_OPTS)")
    lazy val cxfHttpsProxyPort = settingKey[Integer]("set the https proxy port for the wsdl2java command (default from SBT_OPTS)")
    lazy val cxfNoProxy = settingKey[String]("set the noProxy for the wsdl2java command (default from SBT_OPTS)")

    trait ICxfWsdl {
      val key : String
      val args: Seq[String]
      val systemProperties: Seq[String]

      def outputDir(basedir: File) = new File(basedir, key).getAbsoluteFile
    }

    case class CxfWsdl(file: File, args: Seq[String], key: String, systemProperties: Seq[String] = Seq()) extends ICxfWsdl {
    }

    case class CxfWsdlUrl(url: URL, args: Seq[String], key: String, systemProperties: Seq[String] = Seq()) extends ICxfWsdl {
    }
  }

  import autoImport._

  val cxfDefaults: Seq[Def.Setting[_]] = Seq(
    cxfVersion := "3.2.0",
    cxfXjcTsVersion := cxfVersion.value,
    cxfJaxb2BasicsVersion := "1.11.1",
    cxfLogbackVersion := "1.2.3",
    cxfFilePath := crossTarget.value / "cxf",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % cxfLogbackVersion.value % CxfConfig.name,
      "org.apache.cxf" % "cxf-tools-wsdlto-core" % cxfVersion.value % CxfConfig.name,
      "org.apache.cxf" % "cxf-tools-wsdlto-databinding-jaxb" % cxfVersion.value % CxfConfig.name,
      "org.apache.cxf" % "cxf-tools-wsdlto-frontend-jaxws" % cxfVersion.value % CxfConfig.name,
      "org.apache.cxf.xjcplugins" % "cxf-xjc-ts" % cxfVersion.value % CxfConfig.name,
      "org.jvnet.jaxb2_commons" % "jaxb2-basics" % cxfJaxb2BasicsVersion.value % CxfConfig.name
    ),
    cxfWsdls := Nil,
    cxfWsdlsUrls := Nil,
    wsdl2javaDefaultArgs := Seq("-verbose", "-autoNameResolution", "-exsh", "true", "-fe", "jaxws21", "-client"),
    cxfParallelExecution := true,

    cxfHttpProxyHost := System.getProperty("http.proxyHost", ""),
    cxfHttpProxyPort := Integer.getInteger("http.proxyPort", 0),
    cxfHttpsProxyHost := System.getProperty("https.proxyHost", ""),
    cxfHttpsProxyPort := Integer.getInteger("https.proxyPort", 0),
    cxfNoProxy := System.getProperty("http.nonProxyHosts", "")
  )

  private lazy val cxfConfig = Seq(
    // initialisation de la clef correspondante au répertoire source dans lequel les fichiers générés seront copiés
    sourceManaged in CxfConfig := cxfFilePath.value,
    // ajout de ce répertoire dans la liste des répertoires source à prendre en compte lors de la compilation
    managedSourceDirectories in Compile += {
      (sourceManaged in CxfConfig).value
    },
    managedClasspath in wsdl2java := {
      Classpaths.managedJars(CxfConfig, (classpathTypes in wsdl2java).value, update.value)
    },
    // définition de la tâche wsdl2java
    wsdl2java := {
      val s: TaskStreams = streams.value
      val classpath: String = (managedClasspath in wsdl2java).value.files.map(_.getAbsolutePath).mkString(System.getProperty("path.separator"))
      val basedir: File = crossTarget.value / "cxf_tmp"
      IO.createDirectory(basedir)

      def outputDir(wsdl: ICxfWsdl): File = wsdl.outputDir(basedir)

      def processWsdl(wsdl : ICxfWsdl, wsdlPath: String): Unit = {
        val id: String = wsdl.key
        val output = outputDir(wsdl)
        val args: Seq[String] = Seq("-d", output.getAbsolutePath) ++ wsdl2javaDefaultArgs.value ++ wsdl.args :+ wsdlPath
        s.log.debug("Removing output directory for " + id + " ...")
        IO.delete(output)
        s.log.info("Compiling " + id)

        val sysProps = Seq("-Dfile.encoding=UTF-8") ++
          (if(cxfHttpProxyHost.value.length > 0) Seq(s"-Dhttp.proxyHost=${cxfHttpProxyHost.value}" ) else Nil) ++
          (if(cxfHttpProxyPort.value > 0) Seq(s"-Dhttp.proxyPort=${cxfHttpProxyPort.value}") else Nil) ++
          (if(cxfHttpsProxyHost.value.length > 0) Seq(s"-Dhttps.proxyHost=${cxfHttpsProxyHost.value}") else Nil) ++
          (if(cxfHttpsProxyPort.value > 0) Seq(s"-Dhttps.proxyPort=${cxfHttpsProxyPort.value}") else Nil) ++
          (if(cxfNoProxy.value.length > 0) Seq(s"-Dhttp.nonProxyHosts=${cxfNoProxy.value}") else Nil) ++
          wsdl.systemProperties

        val cmd = Seq("java", "-cp", classpath) ++ sysProps ++ Seq("org.apache.cxf.tools.wsdlto.WSDLToJava") ++ args

        s.log.debug(cmd.toString())
        //Since std.Process has been removed in sbt 1.x.x, we have specific logic for the 0.13.x and 1.x.x series.
        ProcessRunner.run(cmd, s.log)
        s.log.info("Finished " + id)
        IO.copyDirectory(output, (sourceManaged in CxfConfig).value, overwrite = true)
      }

      val wsdlColl = if (cxfParallelExecution.value) cxfWsdls.value.par else cxfWsdls.value
      val (updated, notModified) = wsdlColl.partition( wsdl =>
        wsdl.file.lastModified() > outputDir(wsdl).lastModified()
      )

      notModified.foreach(wsdl =>
        s.log.debug("Skipping " + wsdl.key)
      )

      updated.foreach { wsdlFile =>
        processWsdl(wsdlFile, wsdlFile.file.getAbsolutePath)
      }

      val wsdlCollUrl = if (cxfParallelExecution.value) cxfWsdlsUrls.value.par else cxfWsdlsUrls.value
      wsdlCollUrl.foreach { wsdlUrl =>
        processWsdl(wsdlUrl, wsdlUrl.url.toString)
      }

      ((sourceManaged in CxfConfig).value ** "*.java").get
    },
    (sourceGenerators in Compile) += wsdl2java
  )

  override lazy val projectSettings =
    Seq(ivyConfigurations += CxfConfig) ++ cxfDefaults ++ inConfig(Compile)(cxfConfig)
}
