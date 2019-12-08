# refuel-json

```
libraryDependencies += "com.phylage" %% "refuel-json" % "1.0.0-RC4"
```

refuel-json automatically generates codec and supports JSON mutual conversion fast and easy.

## Usage

Generate any codec with CaseClassCodec or ConstCodec.<br/>
Both CaseClassCodec and ConstCodec are automatically generated up to the member's internal Codec.

```scala
class Test extends JsParser {
  // A and B codecs do not need to be declared
  jsonString.as(CaseClassCodec.from[C])
}

case class A(value: String)
case class B(a: A)
case class C(b: B)
```

However, you may want to change the lower codec when generating the upper codec.<br/>
In that case, if an implicit Codec exists in the implicit scope, it will be substituted.

```scala
case class A(value: String)
case class B(a: A)
case class C(b: B)
case class D(c: C)

implicit val B_CODEC: Codec[B] = ???

// In this case, B_CODEC is used internally.
jsonString.as(CaseClassCodec.from[D])
```

`CaseClassCodec.from[T]` generates a Codec only for classes with `apply` and `unapply/unapplySeq`.<br/>
Use ConstCodec if you do not have apply / unapply, such as trait or Factory function.<br/>
Codec is generated by explicitly passing json key name and `apply` / `unapply`.

```scala
trait A {
  val id: String
  val value: Int
}


// In this case, B_CODEC is used internally.
s"""{"id": "abcde", "value_number": 123}""".as(ConstCodec.from("id", "value_number")((a, b) => new A {
val id = a
val value = b
})(z => Some((z.id, z.value))))
```

Similarly, ConstCodec does not need to declare the inner class Codec.<br/>

## Codec build DSL

It is possible to build arbitrary Codec by combining specific Codec.

```
{
  "area1": {
    "parent1": [
      {
        "childId": 1,
        "props": ["xxx", "yyy"]
      },
      {
        "childId": 2,
        "props": ["aaa"],
        "ability": ["???"]
      }
    ],
    "parent3": [
      {
        "childId": 3,
        "props": [],
        "ability": ["???", "???", "???"]
      },
      {
        "childId": 4,
        "props": ["aaa"],
      }
    ]
  }
}
```

```scala
  val parentsCodec = (
    option("parent1".parsed(vector(ChildCodec))) ++
    option("parent1".parsed(vector(ChildCodec))) ++
    option("parent1".parsed(vector(ChildCodec)))
  )(Parents.apply)(Parents.unapply)

  val rootCodec = (
    "area1".parsed(option(parentsCodec)) ++
    "area2".parsed(option(parentsCodec)) ++
    "area3".parsed(option(parentsCodec))
  )(Root.apply)(Root.unapply)
```

In this way, codecs can be generated according to JsonFormat, domain model, etc.

