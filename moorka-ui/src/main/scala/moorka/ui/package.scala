package moorka

import java.util.UUID

import moorka.rx._
import moorka.ui.components.FutureElement
import moorka.ui.components.html.ElementSwitcher
import moorka.ui.element.ElementBase
import vaska.{JSObj, JSAccess}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object ui {

  /**
   * Sometimes we need to have isolated display state
   * for part of the tree.
   * @example
   * div(
   *   img(src := "dot.png"),
   *   block {
   *     val text = Var("")
   *     div(
   *       input(value =:= text),
   *       span(text)
   *     )
   *   }
   * )
   */
  def block(f: => ElementBase): ElementBase = f

  def switch(f: => Rx[ElementBase]): ElementSwitcher = {
    new ElementSwitcher(f)
  }

  private[moorka] var jsAccess: JSAccess = null

  private[moorka] var document: JSObj = null

  lazy val global = jsAccess.obj("global")
  
  // So dirty
  private[moorka] def setJSAccess(value: JSAccess): Future[Unit] = {
    jsAccess = value
    document = value.obj("^document")
    for {
      doc ← value.obj("global").get[JSObj]("document")
      _ ← doc.saveAs("^document")
    } yield {
      ()
    }
  }
  
  trait RefHolder {
    val ref: JSObj
  }

  def Ref(tagName: String): JSObj = {
    val id = UUID.randomUUID().toString
    val obj = jsAccess.obj(id)
    document.callAndSaveAs("createElement", tagName)(id)
    obj.set("id", id)
    obj
  }

  type Component = components.base.Component

  implicit def ToFutureElement[T <: ElementBase](x: Future[T])(implicit ec: ExecutionContext): FutureElement = {
    new FutureElement(x)
  }
}
