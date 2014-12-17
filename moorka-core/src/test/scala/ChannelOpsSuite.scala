import moorka.rx._
import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object ChannelOpsSuite extends TestSuite {
  val tests = TestSuite {
    // Test .subscribe
    "Chanel" - {
      "broadcast event to all subscribers" - {
        val emitter = Channel[String]
        var calls = 0
        emitter.subscribe( _ => calls += 1)
        emitter.subscribe( _ => calls += 1)
        emitter.subscribe( _ => calls += 1)
        emitter.emit("")
        assert(calls == 3)
      }
      "stop broadcasting when slot killed" - {
        val emitter = Channel[String]
        var calls = 0
        emitter.subscribe( _ => calls += 1)
        emitter.subscribe( _ => calls += 1).kill()
        emitter.subscribe( _ => calls += 1).kill()
        emitter.emit("")
        assert(calls == 1)
      }
      "swept by Reaper" - {
        implicit val reaper = Reaper()
        val channel = Channel[Int]
        var calls = 0
        channel.subscribe(_ => calls += 1)
        reaper.sweep()
        channel.emit(0)
        assert(calls == 0)
      }
    }

    "Mapped Channel" - {
      "changes type of event value" - {
        val emitter = Channel[Int]
        var result:String = ""
        emitter.map(_.toString).subscribe(result = _)
        emitter.emit(42)
        assert(result == "42")
      }
      "is killed correctly" - {
        val emitter = Channel[Int]
        emitter.map(_.toString).kill()
        assert(emitter.children.length == 0)
      }
      "swept by Reaper" - {
        val channel = Channel[Int]
        def f() = {
          implicit val reaper = Reaper()
          val mapped = channel.map(_.toString)
          var calls = 0
          mapped.subscribe(_ => calls += 1)
          reaper.sweep()
          channel.emit(0)
          assert(calls == 0)
        }
        f()
      }
    }

    "Filtered Channel" - {
      "blocks events which not satisfy condition" - {
        val emitter = Channel[Int]
        var calls:Int = 0
        emitter
          .filter(x => x != 42)
          .subscribe(_ => calls += 1)
        emitter.emit(42)
        emitter.emit(25)
        assert(calls == 1)
      }
      "is killed correctly" - {
        val emitter = Channel[Int]
        emitter.filter(_ => true).kill()
        assert(emitter.children.length == 0)
      }
      "swept by Reaper" - {
        val channel = Channel[Int]
        def f() = {
          implicit val reaper = Reaper()
          val filtered = channel.filter(_ > 0)
          var calls = 0
          filtered.subscribe(_ => calls += 1)
          reaper.sweep()
          channel.emit(1)
          assert(calls == 0)
        }
        f()
      }
    }
  }
}
