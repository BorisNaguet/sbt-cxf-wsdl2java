package io.github.borisnaguet.sbt.plugins

import sbt._

object ProcessRunner {

  def run(cmd: Seq[String], logger: sbt.Logger) = {
    cmd ! logger
  }

}