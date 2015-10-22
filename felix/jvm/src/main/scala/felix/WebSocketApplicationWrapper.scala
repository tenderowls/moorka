package felix

import moorka.flow.Context
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
    val executionContext: ExecutionContext = webSocketJsAccess.executionContext
    val jsAccess: JSAccess = webSocketJsAccess
    val flowContext = Context()
  }

  val application = factory(system)

  system.document.getAndSaveAs("body", "startupBody") foreach { body ⇒
    webSocketJsAccess.request[Unit]("init") foreach { _ ⇒
      application.beforeStart() foreach { _ ⇒
        body.call[JSObj]("appendChild", application.ref) foreach { _ =>
          body.free()
        }
      }
    }
  }
}
