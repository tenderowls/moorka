package com.tenderowls.moorka

import com.tenderowls.moorka.core.collection.Collection

import scala.scalajs.test.JasmineTest

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object CollectionSuite extends JasmineTest {

  describe("Collection") {

    it("check add += action") {
      val collection = Collection[Int]
      var calls = 0
      collection.added.subscribe( x => calls += 1)
      collection += 1
      expect(1).toEqual(calls)
      expect(1).toEqual(collection(0))
    }

    it("check remove -= action") {
      var calls = 0
      val collection = Collection(1, 2, 3, 4, 5)
      collection.removed subscribe { x =>
        expect(3).toEqual(x.idx)
        calls += 1
      }
      collection -= 4
      expect(1).toEqual(calls)
      expect(4).toEqual(collection.length)
      expect(5).toEqual(collection(3))
    }

    it("check update element action") {
      var calls = 0
      val collection = Collection(1, 2, 3, 4, 5)
      collection.updated subscribe { x =>
        expect(3).toEqual(x.idx)
        calls += 1
      }
      collection(3) = 1
      expect(1).toEqual(calls)
      expect(1).toEqual(collection(3))
    }

    it("check insert element action") {
      var calls = 0
      val collection = Collection(1, 2, 3, 4, 5)
      collection.inserted subscribe { x =>
        expect(2).toEqual(x.idx)
        calls += 1
      }
      collection.insert(2, 10)
      expect(1).toEqual(calls)
      expect(6).toEqual(collection.length)
      expect(10).toEqual(collection(2))
      expect(3).toEqual(collection(3))
    }
  }

  describe("Mapped collection should be changed") {

    def mapFunction(x:Int):String = x match {
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
      case 4 => "four"
      case 5 => "five"
      case 6 => "six"
      case 7 => "seven"
    }

    it("when new element added into parent collection") {
      val collection = Collection(1, 2, 3)
      val mapped = collection.map(mapFunction)
      collection += 4
      expect("four").toEqual(mapped(3))
    }

    it("when element removed from parent collection") {
      val collection = Collection(1, 2, 3)
      val mapped = collection.map(mapFunction)
      collection -= 2
      expect("one").toEqual(mapped(0))
      expect("three").toEqual(mapped(1))
    }

    it("when new element inserted into parent collection") {
      val collection = Collection(1, 3)
      val mapped = collection.map(mapFunction)
      //println(collection)
      //println(mapped)
      collection.insert(1, 2)
      //println(collection)
      //println(mapped)
      expect("one").toEqual(mapped(0))
      expect("two").toEqual(mapped(1))
      expect("three").toEqual(mapped(2))
    }

    it("when element updated in parent collection") {
      val collection = Collection(1, 2)
      val mapped = collection.map(mapFunction)
      collection(0) = 2
      expect("two").toEqual(mapped(0))
    }

    // todo
    // test removed element is a same
  }

  describe("Filtered collection should be") {
    it ("updated when changes of parent collection satisfy to filter") {
      val collection = Collection("John", "Tom", "Jane")
      val filtered = collection.filter(_.startsWith("J"))
      expect(Collection("John", "Jane").toString).toEqual(filtered.toString)
      collection += "Jade"
      expect(3).toEqual(filtered.length)
    }
  }
}
