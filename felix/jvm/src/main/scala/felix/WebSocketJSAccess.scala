package felix

import java.util.concurrent.ForkJoinPool

import org.java_websocket.WebSocket
import vaska.JSAccess

import scala.concurrent.ExecutionContext

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class WebSocketJSAccess(webSocket: WebSocket) extends JSAccess {

  val forkJoinPool = new ForkJoinPool(1)

  implicit val executionContext = ExecutionContext.fromExecutor(forkJoinPool)

  def seqToJSON(xs: Seq[Any]): String = {
    val xs2 = xs map {
      case s: String if !s.startsWith("[") ⇒ "\"" + s + "\""
      case any ⇒ any
    }
    "[" + xs2.reduce(_ + ", " + _) + "]"
  }

  override def platformDependentPack(value: Any): Any = value match {
    case xs: Seq[Any] ⇒ seqToJSON(xs)
    case x ⇒ super.platformDependentPack(x)
  }

  /**
   * Abstract method sends message to remote page
   */
  def send(args: Seq[Any]): Unit = {
    val message = seqToJSON(args)
    webSocket.send(message)
  }

  def receive(message: String): Unit = {
    def prepareString(value: String) = {
      value match {
        case s: String if s.startsWith("\"") ⇒
          s.substring(1, s.length - 1).trim
        case s ⇒ s.trim
      }
    }
    val args = message.
      stripPrefix("[").
      stripSuffix("]").
      split(",").
      map(prepareString)
    val reqId = args(0).toInt
    if (reqId == -1) {
      val callbackId = args(1)
      val arg = args(2)
      fireCallback(callbackId, arg)
    }
    else {
      val isSuccess = args(1).toBoolean
      val res = args(2)
      resolvePromise(reqId, isSuccess, res)
    }
  }
}
