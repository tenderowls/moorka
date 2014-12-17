import moorka.rx._
import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object StateOpsSuite extends TestSuite {
  val tests = TestSuite {
    "Mapped State" - {
      "changes type of event value" - {
        val emitter = Var(42)
        val result = emitter.map(_.toString)
        assert(result() == "42")
      }
      "should kills correctly" - {
        val emitter = Var(0)
        emitter.map(_.toString).kill()
        assert(emitter.children.length == 0)
      }
      "swept by Reaper" - {
        val state = Var(0)
        def f() = {
          var calls = 0
          implicit val reaper = Reaper()
          val mapped = state.map(_.toString)
          mapped.subscribe(_ => calls += 1)
          reaper.sweep()
          state() = 1
          assert(calls == 0)
        }
        f()
      }
    }

    "State observer" - {
      "calls immediately" - {
        val v = Var(42)
        var calls = 0
        v.observe(calls += 1)
        assert(calls == 1)
      }
    }

    "Zipped State" - {
      "emits when any parent was changed" - {
        val state1 = Var("Cat")
        val state2 = Var(1)
        val zipped = state1 zip state2
        var calls = 0
        zipped.subscribe { x =>
          calls += 1
          calls match {
            case 1 => assert(x.toString() == "(Dog,1)")
            case 2 => assert(x.toString() == "(Dog,2)")
          }
        }
        state1() = "Dog"
        state2() = 2
      }

      "is killed correctly" - {
        var calls = 0
        val state1 = Var("Cat")
        val state2 = Var(1)
        val zipped = state1 zip state2
        val slot = zipped.subscribe(_ => calls += 1)
        state1() = "Dog"
        slot.kill()
        state2() = 2
        assert(calls == 1)
      }
    }
  }
}
