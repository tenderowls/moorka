package com.tenderowls.moorka

import com.tenderowls.moorka.core._

import scala.scalajs.test.JasmineTest

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RxStateOpsSuite extends JasmineTest {

  describe("Mapped RxState") {
    it("changes type of event value") {
      val emitter = Var(42)
      val result = emitter.map(_.toString)
      expect("42").toEqual(result())
    }
    it("should kills correctly") {
      val emitter = Var(0)
      emitter.map(_.toString).kill()
      expect(0).toEqual(emitter.children.length)
    }
  }

  describe("RxState observer") {
    it("calls immediately") {
      val v = Var(42)
      var calls = 0
      v.observe(calls += 1)
      expect(1).toEqual(calls)
    }
  }

  describe("Zipped RxStream") {
    it("emits when any parent was changed") {
      val state1 = Var("Cat")
      val state2 = Var(1)
      val zipped = state1 zip state2
      var calls = 0
      zipped.subscribe { x =>
        calls += 1
        calls match {
          case 1 => expect("(Dog,1)").toEqual(x.toString())
          case 2 => expect("(Dog,2)").toEqual(x.toString())
        }
      }
      state1() = "Dog"
      state2() = 2
    }
    // todo test kill
  }
}
