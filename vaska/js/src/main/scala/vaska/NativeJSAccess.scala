package vaska

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * Implementation of JSAccess for using in Scala.js.
 *
 * @param scope Object should contain `postMessage` function.
 *              After initialization NativeJSAccess set `onmessage`
 *              function into scope.
 *
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
@JSExport
final class NativeJSAccess(scope: js.Dynamic) extends JSAccess {

  implicit val executionContext = {
    scala.scalajs.concurrent.JSExecutionContext.runNow
  }

  // Receive messages from page and resolve promises
  scope.onmessage = { event: js.Dynamic ⇒
    val msg = event.data.asInstanceOf[js.Array[Any]]
    val reqId = msg(0).asInstanceOf[Int]
    if (reqId == -1) {
      val callbackId = msg(1).asInstanceOf[String]
      val arg = msg(2)
      fireCallback(callbackId, arg)
    }
    else {
      val isSuccess = msg(1).asInstanceOf[Boolean]
      val res = msg(2)
      resolvePromise(reqId, isSuccess, res)
    }
  }: js.Function1[js.Dynamic, Unit]

  override def platformDependentPack(value: Any): Any = value match {
    case xs: Seq[Any] ⇒ js.Array(xs:_*)
    case x ⇒ super.platformDependentPack(x)
  }

  def send(args: Seq[Any]): Unit = {
    val buffer = js.Array[Any]()
    val transferable = js.Array[Any]()
    args foreach {
      case Transferable(value) ⇒
        buffer.push(value)
        transferable.push(value)
      case value ⇒
        buffer.push(value)
    }
    if (transferable.length > 0) {
      scope.postMessage(buffer, transferable)
    }
    else scope.postMessage(buffer)
  }
}
