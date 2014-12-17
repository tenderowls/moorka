import moorka.rx._
import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object RxStreamOpsSuite extends TestSuite {
  val tests = TestSuite {
    // Test .subscribe
    "RxStream" - {
      "should broadcast event to all subscribers" - {
        val emitter = Channel[String]
        var calls = 0
        emitter.subscribe( _ => calls += 1)
        emitter.subscribe( _ => calls += 1)
        emitter.subscribe( _ => calls += 1)
        emitter.emit("")
        assert(calls == 3)
      }
      "should stop broadcasting when slot killed" - {
        val emitter = Channel[String]
        var calls = 0
        emitter.subscribe( _ => calls += 1)
        emitter.subscribe( _ => calls += 1).kill()
        emitter.subscribe( _ => calls += 1).kill()
        emitter.emit("")
        assert(calls == 1)
      }
    }

    "Mapped RxStream" - {
      "changes type of event value" - {
        val emitter = Channel[Int]
        var result:String = ""
        emitter.map(_.toString).subscribe(result = _)
        emitter.emit(42)
        assert(result == "42")
      }
      "should kills correctly" - {
        val emitter = Channel[Int]
        emitter.map(_.toString).kill()
        assert(emitter.children.length == 0)
      }
    }

    "Filtered RxStream" - {
      "blocks events which not satisfy condition" - {
        val emitter = Channel[Int]
        var calls:Int = 0
        emitter.filter(x => x != 42).subscribe(_ => calls += 1)
        emitter.emit(42)
        emitter.emit(25)
        assert(calls == 1)
      }
      "should kills correctly" - {
        val emitter = Channel[Int]
        emitter.filter(_ => true).kill()
        assert(emitter.children.length == 0)
      }
    }
  }
}
