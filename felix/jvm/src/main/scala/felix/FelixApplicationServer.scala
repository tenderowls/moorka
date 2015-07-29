package felix

import java.net.InetSocketAddress

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

import scala.collection.parallel.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait FelixApplicationServer extends App {

  val applications = mutable.ParHashMap[WebSocket, WebSocketApplicationWrapper]()

  val address = new InetSocketAddress(8180)

  val server = new WebSocketServer(address) {

    def onError(webSocket: WebSocket, e: Exception): Unit = {
      e.printStackTrace()
    }

    def onMessage(webSocket: WebSocket, s: String): Unit = {
      applications.get(webSocket) foreach { wrapper ⇒
        wrapper.webSocketJsAccess.receive(s)
      }
    }

    def onClose(webSocket: WebSocket, i: Int, s: String, b: Boolean): Unit = {
      applications.remove(webSocket) foreach { wrapper ⇒
        wrapper.webSocketJsAccess.forkJoinPool.shutdown()
      }
    }

    def onOpen(webSocket: WebSocket, clientHandshake: ClientHandshake): Unit = {
      val wrapper = new WebSocketApplicationWrapper(webSocket, createApplication)
      applications(webSocket) = wrapper
    }
  }

  server.start()
  println(s"Server started at $address")

  def createApplication(system: FelixSystem): FelixApplication
}
