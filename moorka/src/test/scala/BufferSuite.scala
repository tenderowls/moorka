import moorka._
import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object BufferSuite extends TestSuite {
  val tests = TestSuite {
    'Buffer {
      "must emit `added` when +=" - {
        val collection = Buffer[Int]()
        var calls = 0
        val alive = collection.added.foreach( x => calls += 1)
        collection += 1
        assert(calls == 1)
        assert(collection(0) == 1)
      }

      "check remove -= action" - {
        var calls = 0
        val collection = Buffer(1, 2, 3, 4, 5)
        val alive = collection.removed foreach { x =>
          assert(x.idx == 3)
          calls += 1
        }
        collection -= 4
        assert(calls == 1)
        assert(collection.length == 4)
        assert(collection(3) == 5)
      }

      // todo check remove with function
      
      "check update element action" - {
        var calls = 0
        val collection = Buffer(1, 2, 3, 4, 5)
        val alive = collection.updated foreach { x =>
          assert(x.idx == 3)
          calls += 1
        }
        collection(3) = 1
        assert(calls == 1)
        assert(collection(3) == 1)
      }

      "check insert element action" - {
        var calls = 0
        val collection = Buffer(1, 2, 3, 4, 5)
        val alive = collection.inserted foreach { x =>
          assert(x.idx == 2)
          calls += 1
        }
        collection.insert(2, 10)
        assert(calls == 1)
        assert(collection.length == 6)
        assert(collection(2) == 10)
        assert(collection(3) == 3)
      }
    }

    "Mapped buffer should be changed" - {

      def mapFunction(x:Int):String = x match {
        case 1 => "one"
        case 2 => "two"
        case 3 => "three"
        case 4 => "four"
        case 5 => "five"
        case 6 => "six"
        case 7 => "seven"
      }

      "when new element added into parent collection" - {
        val collection = Buffer(1, 2, 3)
        val mapped = collection.map(mapFunction)
        System.gc()
        collection += 4
        assert(mapped(3) == "four")
      }

      "when element removed from parent collection" - {
        val collection = Buffer(1, 2, 3)
        val mapped = collection.map(mapFunction)
        System.gc()
        collection -= 2
        assert(mapped(0) == "one")
        assert(mapped(1) == "three")
      }

      "when new element inserted into parent collection" - {
        val collection = Buffer(1, 3)
        val mapped = collection.map(mapFunction)
        System.gc()
        //println(collection)
        //println(mapped)
        collection.insert(1, 2)
        //println(collection)
        //println(mapped)
        assert(mapped(0) == "one")
        assert(mapped(1) == "two")
        assert(mapped(2) == "three")
      }

      "when element updated in parent collection" - {
        val collection = Buffer(1, 2)
        val mapped = collection.map(mapFunction)
        System.gc()
        collection(0) = 2
        assert(mapped(0) == "two")
      }
      // todo test removed element is a same
    }

    "Filtered buffer should be" - {
      "updated when changes of parent collection satisfy to filter" - {
        val collection = Buffer("John", "Tom", "Jane")
        val filtered = collection.filter(_.startsWith("J"))
        System.gc()
        assert(filtered.toString == Buffer("John", "Jane").toString)
        collection += "Jade"
        assert(filtered.length == 3)
      }
    }
  }
}
