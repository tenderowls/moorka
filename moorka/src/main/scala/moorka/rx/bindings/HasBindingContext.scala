package moorka.rx.bindings

import moorka.rx.Source

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait HasBindingContext[A] {
  val bindingContext: Option[Binding[_]] = bindingStack.headOption
  val parent: Source[A]
}
