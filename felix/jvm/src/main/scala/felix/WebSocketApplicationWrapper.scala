package felix

import org.java_websocket.WebSocket
import vaska.{JSAccess, JSObj}

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class WebSocketApplicationWrapper(webSocket: WebSocket,
                                  factory: FelixSystem ⇒ FelixApplication) {

  val webSocketJsAccess = new WebSocketJSAccess(webSocket)

  private implicit val ec = webSocketJsAccess.executionContext

  val system = new FelixSystem {
    val ec: ExecutionContext = webSocketJsAccess.executionContext
    val jsAccess: JSAccess = webSocketJsAccess
  }

  val application = factory(system)

  system.document.getAndSaveAs("body", "startupBody") foreach { body ⇒
    body.call[JSObj]("appendChild", application.ref) foreach { _ =>
      webSocketJsAccess.request[Unit]("init") foreach { _ ⇒
        body.free()
      }
    }
  }
}
