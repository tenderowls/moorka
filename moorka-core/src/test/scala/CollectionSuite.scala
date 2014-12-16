import com.tenderowls.moorka.core._
import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object CollectionSuite extends TestSuite {
  val tests = TestSuite {
    'Collection {
      "must emit `added` when +=" - {
        val collection = Collection[Int]
        var calls = 0
        collection.added.subscribe( x => calls += 1)
        collection += 1
        assert(calls == 1)
        assert(collection(0) == 1)
      }

      "check remove -= action" - {
        var calls = 0
        val collection = Collection(1, 2, 3, 4, 5)
        collection.removed subscribe { x =>
          assert(x.idx == 3)
          calls += 1
        }
        collection -= 4
        assert(calls == 1)
        assert(collection.length() == 4)
        assert(collection(3) == 5)
      }

      "check update element action" - {
        var calls = 0
        val collection = Collection(1, 2, 3, 4, 5)
        collection.updated subscribe { x =>
          assert(x.idx == 3)
          calls += 1
        }
        collection(3) = 1
        assert(calls == 1)
        assert(collection(3) == 1)
      }

      "check insert element action" - {
        var calls = 0
        val collection = Collection(1, 2, 3, 4, 5)
        collection.inserted subscribe { x =>
          assert(x.idx == 2)
          calls += 1
        }
        collection.insert(2, 10)
        assert(calls == 1)
        assert(collection.length() == 6)
        assert(collection(2) == 10)
        assert(collection(3) == 3)
      }
    }

    "Mapped collection should be changed" - {

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
        val collection = Collection(1, 2, 3)
        val mapped = collection.map(mapFunction)
        collection += 4
        assert(mapped(3) == "four")
      }

      "when element removed from parent collection" - {
        val collection = Collection(1, 2, 3)
        val mapped = collection.map(mapFunction)
        collection -= 2
        assert(mapped(0) == "one")
        assert(mapped(1) == "three")
      }

      "when new element inserted into parent collection" - {
        val collection = Collection(1, 3)
        val mapped = collection.map(mapFunction)
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
        val collection = Collection(1, 2)
        val mapped = collection.map(mapFunction)
        collection(0) = 2
        assert(mapped(0) == "two")
      }
      // todo test removed element is a same
    }

    "Filtered collection should be" - {
      "updated when changes of parent collection satisfy to filter" - {
        val collection = Collection("John", "Tom", "Jane")
        val filtered = collection.filter(_.startsWith("J"))
        assert(filtered.toString == Collection("John", "Jane").toString)
        collection += "Jade"
        assert(filtered.length() == 3)
      }
    }
  }
}
