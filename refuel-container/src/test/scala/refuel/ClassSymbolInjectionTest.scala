package refuel

import org.scalatest.diagrams.Diagrams
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import refuel.ClassSymbolInjectionTest.TEST_N.{Inner, N1, N2}
import refuel.container.anno.{Effective, RecognizedDynamicInjection}
import refuel.domination.Inject
import refuel.domination.InjectionPriority.{Finally, Overwrite, Primary, Secondary}
import refuel.injector.{AutoInject, Injector}
import refuel.internal.di.Effect
import refuel.provider.Lazy

object ClassSymbolInjectionTest {

  object TEST_A {

    trait A {
      def bool: Boolean
    }

    case object A_TRUE extends A with AutoInject {
      override def bool: Boolean = true
    }

    case object A_EFF_A extends Effect with Injector {
      override def activate: Boolean = inject[A].bool
    }

    case object A_EFF_B extends Effect with Injector {
      override def activate: Boolean = !inject[A].bool
    }

    case object A_EFF_C extends Effect with Injector {
      override def activate: Boolean = !inject[A].bool
    }

    trait A_EFFECTIVE_IF

    @Effective(A_EFF_A)
    class A_EFFECTIVE1 extends A_EFFECTIVE_IF with AutoInject {
      val v: String = "A"
    }

    @Effective(A_EFF_B)
    class A_EFFECTIVE2 extends A_EFFECTIVE_IF with AutoInject {
      val v: String = "B"
    }

    @Effective(A_EFF_C)
    class A_EFFECTIVE3 extends A_EFFECTIVE_IF with AutoInject {
      val v: String = "C"
    }

    @Effective(A_EFF_A)
    @Inject[Primary]
    class A_EFFECTIVE4 extends A_EFFECTIVE_IF with AutoInject {
      val v: String = "AA"
    }

    @Effective(A_EFF_B)
    @Inject[Primary]
    class A_EFFECTIVE5 extends A_EFFECTIVE_IF with AutoInject {
      val v: String = "BB"
    }

  }

  object TEST_B {

    trait B[T[_]] {
      def xxx: T[String]
    }

    case class ID1[+T](value: T)

    case class ID2[+T](value: T)

    class B_IMPL_1() extends B[ID1] with AutoInject {
      override def xxx: ID1[String] = ID1("ID1")
    }

    class B_IMPL_2() extends B[ID2] with AutoInject {
      override def xxx: ID2[String] = ID2("ID2")
    }

  }

  object TEST_C {

    object C_Setting {
      val `type` = 2
    }

    object C_EFFECT_A extends Effect {
      override def activate: Boolean = C_Setting.`type` == 1
    }

    object C_EFFECT_B extends Effect {
      override def activate: Boolean = C_Setting.`type` == 2
    }

    object C_EFFECT_C extends Effect {
      override def activate: Boolean = C_Setting.`type` == 3
    }

    trait Effectives {
      val value: String
    }

    @Effective(C_EFFECT_A)
    class C_EFFECTIVE_A() extends Effectives with AutoInject {
      override val value: String = "I am A"
    }

    @Effective(C_EFFECT_B)
    class C_EFFECTIVE_B() extends Effectives with AutoInject {
      override val value: String = "I am B"
    }

    @Effective(C_EFFECT_C)
    class C_EFFECTIVE_C() extends Effectives with AutoInject {
      override val value: String = "I am C"
    }

  }

  object TEST_D {

    trait D {
      val v: String
    }

    class DImpl extends D with AutoInject {
      val v: String = "DImpl"
    }

    object DImplModule extends D with AutoInject {
      val v: String = "DImplModule"
    }

  }

  object TEST_E {

    trait E {
      val v: String
    }

    class EImpl extends E with AutoInject {
      val v: String = "EImpl"
    }

  }

  object TEST_G {

    trait G_FIRST_PARAM

    object G_FIRST_PARAM_IMPL extends G_FIRST_PARAM with AutoInject

    trait G_TYPE_PARAM

    case class G_TYPE_PARAM_A() extends G_TYPE_PARAM

    trait G_INNER[T] {
      val t: T
    }

    class G_INNER_IMPL extends G_INNER[G_TYPE_PARAM_A] with AutoInject {
      val t: G_TYPE_PARAM_A = G_TYPE_PARAM_A()
    }

    trait G {
      val inner: G_INNER[G_TYPE_PARAM_A]
      val first: G_FIRST_PARAM
    }

    class G_IMPL(val first: G_FIRST_PARAM, val inner: G_INNER[G_TYPE_PARAM_A]) extends G with AutoInject

  }

  object TEST_H {

    trait H

    class HImpl extends H with AutoInject

  }

  object TEST_I {

    trait I

    class IImpl() extends I with AutoInject

  }

  object TEST_J {

    trait J

    case class JImpl() extends J with AutoInject

    type J_ALIAS = J

    case class JAliasImpl() extends J_ALIAS with AutoInject

  }

  object TEST_K {

    trait K

    case class KImpl() extends K with AutoInject

  }

  object TEST_L {

    trait L {
      val value: LInner
    }

    @RecognizedDynamicInjection
    trait LInner

    case class LImpl(value: LInner) extends L with AutoInject

  }

  object TEST_M {

    case class M() extends AutoInject

  }

  object TEST_N {
    trait Inner extends AutoInject {
      val value = 1
    }
    object InnerImpl            extends Inner
    case class N1(inner: Inner) extends AutoInject
    class N2(val n1: Lazy[N1])  extends AutoInject
  }

  object TEST_O {
    trait O

    @Inject[Finally]
    class OImpl1 extends O with AutoInject
    class OImpl2 extends O
  }

  object TEST_P {
    trait P

    @Inject[Finally]
    class PImpl1 extends P with AutoInject
    class PImpl2 extends P
  }
}

class ClassSymbolInjectionTest extends AsyncWordSpec with Matchers with Diagrams with Injector {
  "inject" should {
    "Effective injection vs Modify injection priority" in {
      closed { implicit c =>
        import refuel.ClassSymbolInjectionTest.TEST_A._
        inject[A_EFFECTIVE_IF]._provide.isInstanceOf[A_EFFECTIVE4] shouldBe true
      }
    }
    "Reserved type injection" in {
      overwrite(Seq(11))
      inject[Seq[Int] @RecognizedDynamicInjection]._provide shouldBe Seq(11)
    }
    "higher kind injection" in {
      import refuel.ClassSymbolInjectionTest.TEST_B._
      val xx: B[ID1] = inject[B[ID1]]
      inject[B[ID1]].xxx shouldBe ID1("ID1")
      inject[B[ID2]].xxx shouldBe ID2("ID2")
    }
    "Effective injection" in {
      import refuel.ClassSymbolInjectionTest.TEST_C._
      inject[Effectives].value shouldBe "I am B"
    }
    "Auto injectable class vs module" in {
      assertDoesNotCompile("inject[D]._provide")
    }
    "class symbol inject with flush container" in {
      import refuel.ClassSymbolInjectionTest.TEST_E._
      val a = inject[E]._provide
      val b = inject[E]._provide
      a shouldBe b
    }

    "If primary constructor has parameters, inject[T] is recursive inject" in {
      import refuel.ClassSymbolInjectionTest.TEST_G._

      val result = inject[G]._provide
      result.inner.t shouldBe G_TYPE_PARAM_A()
      result.first shouldBe G_FIRST_PARAM_IMPL
    }

    "If primary constructor has parameters, and not found target, inject[T] is fail" in {
      assertDoesNotCompile("inject[K]._provide")
    }

    "Lazy primary constructor injection" in {
      import refuel.ClassSymbolInjectionTest.TEST_K._
      val k: K = inject[Lazy[K]]
      k shouldBe KImpl()
    }

    "If primary constructor has parameters, and bounds are specified, switched inject result." in {
      import refuel.ClassSymbolInjectionTest.TEST_L._

      val inner = new LInner {}
      narrow[LInner](inner).accept(this).indexing()
      inject[L]._provide.value shouldBe inner
    }

    "Unused interfaces." in {
      import refuel.ClassSymbolInjectionTest.TEST_M._

      inject[M]._provide shouldBe M()
    }

    "Internal lazy injection" in {
      def nest(n2: N2): Int = {
        shade { implicit ctn2 =>
          N1(
            new Inner {
              override val value = 3
            }
          ).index(Secondary)

          n2.n1.inner.value
        }
      }

      import refuel.ClassSymbolInjectionTest.TEST_N._

      val n2: N2 = inject[N2]
      n2.n1.inner.value shouldBe 1

      shade { implicit ctn =>
        N1(
          new Inner {
            override val value = 2
          }
        ).index(Overwrite)

        n2.n1.inner.value shouldBe 2

        nest(n2) shouldBe 3

        n2.n1.inner.value shouldBe 2
      }

      n2.n1.inner.value shouldBe 1
    }
  }

  "shade" should {
    "Auto Injectable class symbol inject become a different instance in shade" in {
      import refuel.ClassSymbolInjectionTest.TEST_H._
      val pre = inject[H]._provide
      pre shouldBe inject[H]._provide

      shade { implicit c => pre shouldBe inject[H]._provide }

      pre shouldBe inject[H]._provide
    }

    "Unpropagate indexing on shading containers" in {
      import refuel.ClassSymbolInjectionTest.TEST_O._
      val pre = inject[O]._provide
      pre shouldBe inject[O]._provide

      shade { implicit c =>
        pre shouldBe inject[O]._provide
        new OImpl2().index[O](Primary)
        c.fully[O].size shouldBe 1
        _cntRef.fully[O].size shouldBe 1
        pre shouldNot be(inject[O]._provide)
      }

      _cntRef.fully[O].size shouldBe 1
      pre shouldBe inject[O]._provide
    }

    "Closed new container scope verification." in {
      import refuel.ClassSymbolInjectionTest.TEST_P._
      val pre = inject[P]._provide
      pre shouldBe inject[P]._provide

      closed { implicit c =>
        pre shouldNot be(inject[P]._provide)
        new PImpl2().index[P](Primary)
        c.fully[P].size shouldBe 2
        _cntRef.fully[P].size shouldBe 1
        pre shouldNot be(inject[P]._provide)
      }

      _cntRef.fully[P].size shouldBe 1
      pre shouldBe inject[P]._provide
    }
  }
}
