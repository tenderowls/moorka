package com.tenderowls.moorka.ui

import com.tenderowls.moorka.core.RxStream
import com.tenderowls.moorka.core.rx.Emitter

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js

/**
 * Render bac kend interface
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExport("renderBackendApi")
object RenderAPI {

  type Message = js.Array[Any]

  type WorkerCallback = js.Function1[js.Array[Message], _]

  private val messageBuffer = new js.Array[Message]()

  private var inAction = false

  private var postMessage:WorkerCallback = null

  private val _onMessage = Emitter[Message]
  
  /**
   * Initialize renderBackend for worker mode
   */
  @JSExport def workerMode() = {
    postMessage = js.Dynamic.global.postMessage.asInstanceOf[WorkerCallback]
    js.Dynamic.global.updateDynamic("onmessage")( { x: Any =>
      _onMessage.emit(x.asInstanceOf[js.Dynamic].data.asInstanceOf[Message])
    }: WorkerCallback)
  }

  /**
   * Initialize default render backend
   */
  @JSExport def defaultMode(incoming:WorkerCallback): js.Function1[Message, _] = {
    postMessage = incoming
    (message: Message) => Future {
      _onMessage.emit(message)
    }
  }

  def !(msg: Message) = {
    messageBuffer.push(msg)
    if (!inAction) {
      inAction = true
      Future {
        postMessage(messageBuffer)
        messageBuffer.splice(0)
        inAction = false
      }
    }
  }

  val onMessage: RxStream[Message] = _onMessage
}