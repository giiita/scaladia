package refuel.json.codecs.builder.context.keylit

import refuel.internal.json.codec.builder.JsKeyLitOps
import refuel.json.Json

case object SelfCirculationLit extends JsKeyLitOps with KeyLitParser {
  val v: Seq[String] = Nil

  override def rec(x: Json): Seq[Json] = Seq(x)

  def ++(that: JsKeyLitOps): JsKeyLitOps = this
}