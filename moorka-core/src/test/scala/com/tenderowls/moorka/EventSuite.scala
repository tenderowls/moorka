package com.tenderowls.moorka

import com.tenderowls.moorka

import scala.scalajs.test.JasmineTest

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object EventSuite extends JasmineTest {

  describe("Emitter") {
    it("should broadcast event to all subscribers") {
      val emitter = Emitter[Unit]
      var calls = 0
      emitter.subscribe( _ => calls += 1)
      emitter.subscribe( _ => calls += 1)
      emitter.subscribe( _ => calls += 1)
      emitter.emit()
      expect(3).toEqual(calls)
    }
    it("should stop broadcasting when slot killed") {
      val emitter = Emitter[Unit]
      var calls = 0
      emitter.subscribe( _ => calls += 1)
      emitter.subscribe( _ => calls += 1).kill()
      emitter.subscribe( _ => calls += 1).kill()
      emitter.emit()
      expect(1).toEqual(calls)
    }
  }

  describe("Mapped emitter") {
    it("changes type of event value") {
      val emitter = Emitter[Int]
      var result:String = ""
      emitter.map(_.toString).subscribe(result = _)
      emitter.emit(42)
      expect("42").toEqual(result)
    }
    it("should kills correctly") {
      val emitter = Emitter[Int]
      emitter.map(_.toString).kill()
      expect(0).toEqual(emitter.numSubscribers)
    }
  }

  describe("Filtered emitter") {
    it("blocks events which not satisfy condition") {
      val emitter = Emitter[Int]
      var calls:Int = 0
      emitter.filter(x => x != 42).subscribe(_ => calls += 1)
      emitter.emit(42)
      emitter.emit(25)
      expect(1).toEqual(calls)
    }
    it("should kills correctly") {
      val emitter = Emitter[Int]
      emitter.filter(_ => true).kill()
      expect(0).toEqual(emitter.numSubscribers)
    }
  }

//  xdescribe("Expression bindings") {
//    it("should emit event when one of dependencies changed") {
//      var calls = 0
//      val a = moorka.Var(10)
//      val b = moorka.Var(20)
//      val c = Bind { calls += 1; a() + b() }
//      expect(1).toBe(calls)
//    }
//  }
}
