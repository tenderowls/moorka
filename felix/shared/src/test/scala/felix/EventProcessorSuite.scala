package felix

import felix.core.{EventAction, EventProcessor, EventTarget}
import utest._
import vaska.JSAccess

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object EventProcessorSuite extends TestSuite {

  sealed trait Action
  case class ResolePromise(reqId: Int, isSuccess: Boolean, res: Any) extends Action
  case class FireCallback(cbId: String, res: Any) extends Action
  case object DoNothing extends Action

  def div(id: String, divs: EventTarget*)(implicit ep: EventProcessor) = new EventTarget {
    def refId: String = id
    divs.foreach(_.parent = Some(this))
    ep.registerElement(this)
  }


  def createProcessor(f: Seq[Any] ⇒ Action): (JSAccess, EventProcessor) = {

    val jsAccess = new JSAccess {
      implicit val executionContext: ExecutionContext = utest.ExecutionContext.RunNow
      def send(args: Seq[Any]): Unit = {
        //jsAccess.fireCallback()
        f(args) match {
          case ResolePromise(a,b,c) ⇒ resolvePromise(a,b,c)
          case FireCallback(a,b) ⇒ fireCallback(a,b)
          case DoNothing ⇒
        }
      }
    }
    (jsAccess, new EventProcessor(
      jsAccess,
      jsAccess.obj("document"),
      jsAccess.obj("utils")
    )(utest.ExecutionContext.RunNow))
  }

  val (jsAccess, ep) = createProcessor {
    case Seq(id: Int, "get", "@link:event", "type") ⇒
      ResolePromise(id, isSuccess = true, "event")
    case Seq(id: Int, "get", "@link:event", "target") ⇒
      ResolePromise(id, isSuccess = true, "@obj:el1.2.1")
    case Seq(id: Int, "get", "@link:el1.2.1", "id") ⇒
      ResolePromise(id, isSuccess = true, "el1.2.1")
    case seq ⇒
      println(s"? $seq")
      DoNothing
  }
  implicit val epi = ep
  val el121 = div("el1.2.1")
  val el12 = div("el1.2",
    el121
  )
  val el1 = div("el1",
    div("el1.1"), el12
  )

  def tests = TestSuite {
    "check at target" - {
      var listenCalls = 0
      ep.addListener(el121, "event", (_, _, _) ⇒ listenCalls += 1)
      jsAccess.fireCallback("^cb1", jsAccess.obj("event"))
      assert(listenCalls == 1)
    }
    "check bubbling" - {
      var listenCalls = 0
      ep.addListener(el12, "event", (_, _, _) ⇒ listenCalls += 1)
      jsAccess.fireCallback("^cb1", jsAccess.obj("event"))
      assert(listenCalls == 1)
    }
    "check capturing" - {
      var captureCalls = 0
      ep.addCapture(el1, "event", (_, _, _) ⇒ captureCalls += 1)
      jsAccess.fireCallback("^cb1", jsAccess.obj("event"))
      assert(captureCalls == 1)
    }
    "check complex behavior" - {
      var listenCalls = 0
      var captureCalls = 0
      ep.addListener(el121, "event", (_, _, _) ⇒ listenCalls += 1)
      ep.addListener(el12, "event", (_, _, _) ⇒ listenCalls += 1)
      ep.addCapture(el1, "event", (_, _, _) ⇒ captureCalls += 1)
      jsAccess.fireCallback("^cb1", jsAccess.obj("event"))
      assert(listenCalls == 2)
      assert(captureCalls == 1)
    }
    "check stop propagation" - {
      var captureCalls = 0
      var listenCalls = 0
      ep.addCapture(el1, "event", (_, _, _) ⇒ {
        captureCalls += 1
        EventAction.StopPropagation
      })
      ep.addListener(el121, "event", (_, _, _) ⇒ listenCalls += 1)
      jsAccess.fireCallback("^cb1", jsAccess.obj("event"))
      assert(captureCalls == 1)
      assert(listenCalls == 0)
    }
  }

}
