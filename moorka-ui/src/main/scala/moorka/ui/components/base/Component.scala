package moorka.ui.components.base

import moorka.rx.State
import moorka.ui.Ref
import moorka.ui.element.ElementBase

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Component[A] extends Block {
  val state: State[A]
}
