package refuel.json.codecs.builder.context

import refuel.internal.json.codec.builder.JsonKeyRef
import refuel.json.codecs.{CodecTyper, Read, Write}
import refuel.json.{Codec, JsonVal}

trait DynamicCodecGenFeature {
  def Serialize[T](f: T => JsonVal): Write[T]                     = f(_)
  def Deserialize[T](f: JsonVal => T): Read[T]                    = f(_)
  def Format[T](apl: JsonVal => T)(unapl: T => JsonVal): Codec[T] = apl -> unapl

  def ReadWith[T](key: JsonKeyRef)(implicit codec: Read[T]): Read[T] = {
    implicitly[CodecTyper[Read]].wrap(key)(codec)
  }
  def WriteWith[T](key: JsonKeyRef)(implicit codec: Write[T]): Write[T] = {
    implicitly[CodecTyper[Write]].wrap(key)(codec)
  }
  def BothWith[T](key: JsonKeyRef)(implicit codec: Codec[T]): Codec[T] = {
    implicitly[CodecTyper[Codec]].wrap(key)(codec)
  }

  implicit class Fragment[X, T[X]](cf: T[X])(implicit ct: CodecTyper[T]) {
    def raise: Codec[X] = new Codec[X] {
      override def serialize(t: X): JsonVal    = ct.write[X](t)(cf)
      override def deserialize(bf: JsonVal): X = ct.read(bf)(cf)
    }
  }

  implicit def __readRef[T](implicit _c: Codec[T]): Read[T]   = _c
  implicit def __writeRef[T](implicit _c: Codec[T]): Write[T] = _c
}
