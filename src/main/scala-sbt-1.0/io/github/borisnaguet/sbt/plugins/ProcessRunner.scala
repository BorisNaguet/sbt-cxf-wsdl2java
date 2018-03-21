package io.github.borisnaguet.sbt.plugins

import sbt.internal.util.ManagedLogger
import scala.sys.process._

object ProcessRunner {

  def run(cmd: Seq[String], logger: ManagedLogger) = {
    cmd ! logger
  }

}