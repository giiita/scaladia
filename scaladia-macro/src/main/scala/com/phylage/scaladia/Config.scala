package com.phylage.scaladia

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.util.Try

object Config {

  trait SkippedPackage {
    val `package`: String
  }

  case class DefaultPackage(`package`: String) extends SkippedPackage

  case class AdditionalPackage(`package`: String) extends SkippedPackage

  implicit class RichPackageString(value: String) {
    def asDefault: SkippedPackage = DefaultPackage(value)

    def asAddition: SkippedPackage = AdditionalPackage(value)
  }

  val blackList: List[SkippedPackage] = Try {
    ConfigFactory.load(getClass.getClassLoader, "di.conf")
      .getStringList("unscaning.package")
      .asScala.toList.map(_.asAddition) ++ List(
      "com.oracle",
      "com.apple",
      "com.sun",
      "oracle",
      "apple",
      "scala",
      "javafx",
      "javax",
      "sun",
      "java",
      "jdk",
      "<empty>"
    ).map(_.asDefault)
  } getOrElse List.empty
}
