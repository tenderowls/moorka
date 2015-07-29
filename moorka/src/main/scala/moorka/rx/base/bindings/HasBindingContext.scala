package moorka.rx.base.bindings

import moorka.rx.base.Source

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait HasBindingContext[A] {
  val bindingContext: Option[Binding[_]] = bindingStack.headOption
  val parent: Source[A]
}
