package com.tenderowls.moorka.ui

import com.tenderowls.moorka.core.Event
import com.tenderowls.moorka.core.events.Emitter
import org.scalajs.dom

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
    val f = { (message: Message) =>
      dom.setTimeout( { () =>
        _onMessage.emit(message)
      }, 1)
    }
    f
  }

  def !(msg: Message) = {
    messageBuffer.push(msg)
    if (!inAction) {
      inAction = true
      dom.setTimeout( { () =>
        postMessage(messageBuffer)
        messageBuffer.splice(0)
        inAction = false
      }, 1)
    }
  }

  val onMessage: Event[Message] = _onMessage
}