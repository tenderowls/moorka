package com.tenderowls.moorka

import com.tenderowls.moorka.core._

import scala.scalajs.test.JasmineTest

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RxStreamOpsSuite extends JasmineTest {

  // Test .subscribe
  describe("RxStream") {
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

  describe("Mapped RxStream") {
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
      expect(0).toEqual(emitter.children.length)
    }
  }

  describe("Filtered RxStream") {
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
      expect(0).toEqual(emitter.children.length)
    }
  }
}
