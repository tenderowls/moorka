package moorka

import moorka.rx.State
import moorka.ui.components.html.ElementSwitcher
import moorka.ui.element.ElementBase

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

  def switch(f: => State[ElementBase]): ElementSwitcher = {
    new ElementSwitcher(f)
  }
  
  type Ref = render.Ref
  type RefHolder = render.RefHolder
  type Component = components.base.Component

  val RenderAPI = render.RenderAPI
  val Ref = render.Ref

  implicit def FromComponentToElement(x: Component): ElementBase = x.el
}