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
final class NativeJSAccess(scope: js.Dynamic = js.Dynamic.global)
  extends JSAccess {

  implicit val executionContext = {
    scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
  }

  // Receive messages from page and resolve promises
  scope.onmessage = { msg: js.Array[Any] ⇒
    val reqId = msg(0).asInstanceOf[Int]
    val isSuccess = msg(1).asInstanceOf[Boolean]
    val res = msg(2)
    resolvePromise(reqId, isSuccess, res)
  }

  def send(args: Seq[Any]): Unit = {
    val buffer = js.Array(args:_*)
    scope.postMessage(buffer)
  }
}
