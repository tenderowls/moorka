package moorka.rx.base.bindings

import moorka.rx.base.Source

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait DependentBinding[A] extends Binding[A] {
  val parent: Source[A]
}
