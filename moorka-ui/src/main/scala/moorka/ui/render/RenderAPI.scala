package moorka.ui.render

import moorka.rx._

import scala.concurrent.{Promise, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * Render bac kend interface
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExport("renderBackendApi")
object RenderAPI {

  type Message = js.Array[Any]

  type WorkerCallback = js.Function1[js.Array[Message], _]

  private var messageBuffer = new js.Array[Message]()

  private var inAction = false

  private var postMessage:WorkerCallback = null

  private val _onMessage = Channel[Message]
  
  /**
   * Initialize renderBackend for worker mode
   */
  @JSExport def workerMode() = {
    postMessage = js.Dynamic.global.postMessage.asInstanceOf[WorkerCallback]
    js.Dynamic.global.updateDynamic("onmessage")( { x: Any =>
      _onMessage.pull(x.asInstanceOf[js.Dynamic].data.asInstanceOf[Message])
    }: WorkerCallback)
  }

  /**
   * Initialize default render backend
   */
  @JSExport def defaultMode(incoming:WorkerCallback): js.Function1[Message, _] = {
    postMessage = incoming
    (message: Message) => Future {
      _onMessage.pull(message)
    }
  }

  def !(msg: Message) = {
    messageBuffer.push(msg)
    if (!inAction) {
      inAction = true
      Future {
        postMessage(messageBuffer)
        messageBuffer = new js.Array[Message]
        inAction = false
      }
    }
  }

  def ?(msg: Message) = {
    val p = Promise[Any]()
    val pair = (msg(3).toString, p)
    println(s"promise ${msg(3).toString} added")
    promises = promises + pair
    this ! msg
    p.future
  }

  private var promises = Map.empty[Any, Promise[Any]]
  
  private val messageListener = _onMessage foreach { ops ⇒
    val second = ops(1)
    if (second != null) {
      val s = second.toString
      promises.get(s) match {
        case Some(promise) ⇒
          promise.success(ops(2))
          promises -= s
        case None ⇒
      }
    }
  }  

  val onMessage: Channel[Message] = _onMessage
}
