package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.core.Event
import com.tenderowls.moorka.core.events.Emitter
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * Render backend interface
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExport("renderBackend")
object RenderBackendApi {

  type Message = js.Array[Any]

  type WorkerCallback = js.Function1[js.Array[Message], _]

  private val messageBuffer = new js.Array[Message]()

  private var inAction = false

  private var postMessage:WorkerCallback = null

  private var _onMessage: Event[Message] = null
  
  /**
   * Initialize renderBackend for worker mode
   */
  @JSExport def workerMode() = {
    postMessage = js.Dynamic.global.postMessage.asInstanceOf[WorkerCallback]
    val emitter = Emitter[Message]
    _onMessage = emitter
    js.Dynamic.global.updateDynamic("onmessage")( { x: Any =>
      emitter.emit(x.asInstanceOf[js.Dynamic].data.asInstanceOf[Message])
    }: WorkerCallback)
  }

  /**
   * Initialize default render backend
   */
  def defaultMode(incoming:WorkerCallback, outgoing: Event[Message]) = {
    postMessage = incoming
    _onMessage = outgoing
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

  def onMessage: Event[Message] = _onMessage 
}