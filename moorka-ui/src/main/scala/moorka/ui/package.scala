package moorka

import moorka.rx._
import moorka.ui.components.FutureElement
import moorka.ui.components.html.ElementSwitcher
import moorka.ui.element.ElementBase

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

  type Ref = render.Ref
  type RefHolder = render.RefHolder
  type Component = components.base.Component

  val RenderAPI = render.RenderAPI
  val Ref = render.Ref

  implicit def ToFutureElement[T <: ElementBase](x: Future[T])(implicit ec: ExecutionContext): FutureElement = {
    new FutureElement(x)
  }
}
