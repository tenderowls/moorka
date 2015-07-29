
import moorka._
import utest._

import utest.ExecutionContext.RunNow
import scala.concurrent.Promise
import scala.util.Success

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object FSMSuite extends TestSuite {
  val tests = TestSuite {
    def complexWithModBehavior() = {
      val promise = Promise[Boolean]()
      val dependency = Var[String]("AIdle")
      val collectDependency = {
        dependency collect {
          case x if x startsWith "AProgress" ⇒ "BProgress"
          case "ASuccess" ⇒ "BSuccess"
        }
      }
      val res = FSM("BIdle") {
        case "BIdle" =>
          promise.future.toRx.flatMap {
            case Success(true) ⇒ collectDependency
            case _ ⇒ Val("BFailure")
          }
        case "BProgress" ⇒ collectDependency
        case "BSuccess" ⇒ Killer
        case "BFailure" ⇒ Killer
      }
      (promise, dependency, res)
    }

    "check complex withMod behavior 1" - {
      val (promise, dependency, res) = complexWithModBehavior()
      promise.success(true)
      dependency.pull(Val("AProgress 10"))
      dependency.pull(Val("AProgress 12"))
      dependency.pull(Val("AProgress 24"))
      dependency.pull(Val("AProgress 100"))
      dependency.pull(Val("ASuccess"))

      res once { x ⇒
        assert(x == "BSuccess")
        assert(!res.alive)
      }
    }

    "check complex withMod behavior 2" - {
      val (promise, _, res) = complexWithModBehavior()
      promise.success(false)
      res once { x ⇒
        assert(x == "BFailure")
        assert(!res.alive)
      }
    }

    "check Var ignore side effects when state was changed twice" - {
      val state = Var(0d)
      val changesChannel1 = Channel.signal()
      val changesChannel2 = state.stateless
      var bug = false
      FSM(0) {
        case 0 ⇒
          changesChannel1 or changesChannel2 map {
            case Left(_) ⇒ 2
            case Right(_) ⇒ 1
          }
        case 1 ⇒ // Writing
          bug = true
          Dummy
        case 2 ⇒ // Updating
          Val(0)
      } foreach {
        case 2 ⇒
          state.pull(Silent(Math.random()))
        case _ ⇒
      }
      changesChannel1.fire()
      changesChannel1.fire()
      assert(!bug)
    }
  }
}
