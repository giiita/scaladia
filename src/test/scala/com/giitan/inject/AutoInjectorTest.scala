package com.giitan.inject

import com.giitan.injector.Injector
import org.scalatest.{Assertion, Matchers, WordSpec}


class AutoInjectorTest extends WordSpec with Matchers {
  "Auto injector test" should {
    "the simplest Injection" in {
      object Execute extends Injector {
        def test(): Assertion = {
          inject[AutoVariable] shouldBe AutoVariable
        }
      }

      Execute.test()
    }
  }
}
