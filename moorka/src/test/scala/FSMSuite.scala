
import moorka.death.Reaper
import moorka.flow.{Flow, Context}
import moorka.flow.immutable.{Dummy, Val}
import moorka.flow.mutable.{Fsm, Signal, Var}

import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global

import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object FSMSuite extends TestSuite {

  implicit val reaper = Reaper()

  def withContext[U](f: Context ⇒ U): Any = {
    val ctx = Context()
    f(ctx)
    ctx.validate()
  }

  val tests = TestSuite {
    def complexWithModBehavior()(implicit ctx: Context) = {
      val promise = Promise[Boolean]()
      val dependency = Var[String]("AIdle")
      val collectDependency = {
        dependency collect {
          case x if x startsWith "AProgress" ⇒ "BProgress"
          case "ASuccess" ⇒ "BSuccess"
        }
      }
      val res = Fsm("BIdle") {
        case "BIdle" =>
          import moorka.flow.converters.future
          val flowFuture = promise.future.toFlow
          flowFuture flatMap {
            case true ⇒ collectDependency
            case _ ⇒ Val("BFailure")
          } recover {
            case _ ⇒ "BFailure"
          }
        case "BProgress" ⇒ collectDependency
        case "BSuccess" ⇒ Dummy
        case "BFailure" ⇒ Dummy
      }
      (promise, dependency, res)
    }

    "check complex withMod behavior 1" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val (promise, dependency, res) = complexWithModBehavior()
        dependency() = "AProgress 10"
        dependency() = "AProgress 12"
        dependency() = "AProgress 24"
        dependency() = "AProgress 100"
        dependency() = "ASuccess"
        res foreach { x ⇒
          calls += 1
          calls match {
            case 1 ⇒ "BProgress"
            case 2 ⇒ "BSuccess"
          }
        }
        promise.success(true)
      }
      assert(calls == 2)
    }

    "check complex withMod behavior 2" - {
      var calls = 0
      withContext { implicit ctx ⇒
        val (promise, _, res) = complexWithModBehavior()
        promise.success(false)
        res foreach { x ⇒
          assert(x == "BFailure")
          calls += 1
          //assert(!res.alive)
        }
      }
      assert(calls == 1)
    }

    "check Var ignore side effects when state was changed twice" - {
      var bug = false
      withContext { implicit ctx ⇒
        val state = Var(0d)
        val changesChannel1 = Signal()
        val changesChannel2 = state
        val fsm = Fsm(0) {
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
        }

        fsm foreach {
          case 2 ⇒
            //println("2 it works!")
            state() = Math.random()
          case x ⇒
            //println(s"$x it works too!")
        }
        changesChannel1.fire()
        changesChannel1.fire()
      }
      assert(!bug)
    }
  }
}
