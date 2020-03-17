package refuel.json.codecs.factory

import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import refuel.internal.json.codec.builder.JsKeyLitOps
import refuel.json.codecs.All
import refuel.json.codecs.builder.context.keylit.SelfCirculationLit
import refuel.json.codecs.factory.CaseClassCodecTest._
import refuel.json.entry.{JsAnyVal, JsObject, JsString}
import refuel.json.error.DeserializeFailed
import refuel.json.{Codec, CodecDef, JsonVal, JsonTransform}

object CaseClassCodecTest {

  case class BBBB(id: Long, value: String)

  case class CCCC(value: Map[String, BBBB])

  case class DDDD(id: Long, value: CCCC)

  case class A(value: String)

  case class B(a: A)

  case class C(b: B)

  case class D(c: C)

  case class AAA(aaaId: Long, int: Int)

  class BBB(val bbbId: Long, val aaa: Option[AAA]) {
    override def hashCode(): Int = 31 * 1

    override def equals(obj: Any): Boolean = obj match {
      case x: BBB => bbbId == x.bbbId && aaa == x.aaa
      case _ => false
    }
  }

  case class CCC(cccId: Long, bbbs: Seq[BBB])

  case class DDD(dddId: Long, ccc: CCC)

  case class EEE(eeeId: Long, ddd: DDD)

}

class CaseClassCodecTest
  extends AsyncWordSpec with Matchers with Diagrams
    with JsonTransform with CodecDef {

  implicit val aLocalCodec: Codec[A] = CaseClassCodec.from[A]
  implicit val bLocalCodec: Codec[B] = CaseClassCodec.from[B]
  implicit val cLocalCodec: Codec[C] = CaseClassCodec.from[C]

  implicit def _bCodec: Codec[BBB] = new Codec[BBB] with All {
    override def deserialize(bf: JsonVal): Either[DeserializeFailed, BBB] = {
      for {
        a <- bf.named("bbbId").to(implicitly[Codec[Long]]).right
        b <- bf.named("aaa").to(OptionCodec(CaseClassCodec.from[AAA])).right
      } yield new BBB(a, b)
    }

    override def serialize(t: BBB): JsonVal = {
      implicit val _ = CaseClassCodec.from[AAA]
      JsObject()
        .++(JsString("bbbId"))
        .++(JsAnyVal(t.bbbId))
        .++(JsString("aaa"))
        .++(CaseClassCodec.from[Option[AAA]].serialize(t.aaa))
    }

    override def keyLiteralRef: JsKeyLitOps = SelfCirculationLit
  }

  "Load implicit codec" should {
    "single member codec type" in {

      s"""{"c": {"b": {"a": {"value": "hoge"}}}}""".as(CaseClassCodec.from[D]) match {
        case Left(e) => fail(e)
        case Right(x) => x shouldBe D(C(B(A("hoge"))))
      }
    }
    "custom implicit codec type" in {
      val r =
        s"""{"eeeId":1,"ddd":{"dddId":2,"ccc":{"cccId":3,"bbbs":[{"bbbId":4},{"bbbId":5,"aaa":{"aaaId":7,"int":8}},{"bbbId":6}]}}}"""
          .as(CaseClassCodec.from[EEE])
          .right
          .get

      r.eeeId shouldBe 1
      r.ddd.dddId shouldBe 2
      r.ddd.ccc.cccId shouldBe 3
      r.ddd.ccc.bbbs.map(_.bbbId) shouldBe Seq(4, 5, 6)
      r.ddd.ccc.bbbs.map(_.aaa) shouldBe Seq(None, Some(AAA(7, 8)), None)
    }

    "implicit override codec" in {
      val bbbs =
        Seq(new BBB(4, None), new BBB(5, Some(AAA(7, 8))), new BBB(6, None))

      implicit def _ddd: Codec[DDD] = new Codec[DDD] {
        override def serialize(t: DDD): JsonVal = ???

        override def deserialize(bf: JsonVal): Either[DeserializeFailed, DDD] =
          Right(DDD(2, CCC(3, bbbs)))

        override def keyLiteralRef: JsKeyLitOps = SelfCirculationLit
      }

      s"""{"eeeId":1,"ddd":{"overwrite":"insertion value"}}""".as(
        CaseClassCodec.from[EEE]
      ) shouldBe Right {
        EEE(1, DDD(2, CCC(3, bbbs)))
      }
    }

    "Used implicitly codec of refuel" in {
      s"""{"id": 0, "value": {"value": {"hoge": {"id": 1, "value": "AAA"}, "huga": {"id": 2, "value": "BBB"}}}}"""
        .as(CaseClassCodec.from[DDDD]) match {
        case Left(_) => fail()
        case Right(r) =>
          r shouldBe DDDD(
            0,
            CCCC(Map("hoge" -> BBBB(1, "AAA"), "huga" -> BBBB(2, "BBB")))
          )
      }
    }
  }
}
