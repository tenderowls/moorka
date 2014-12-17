package moorka.ui.components.base

import moorka.rx.RxState
import moorka.ui.Ref
import moorka.ui.element.ElementBase

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Component[State] extends Block {
  val state: RxState[State]
}
