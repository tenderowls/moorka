package moorka.rx.base

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait State[+A] extends Channel[A] {
  def apply(): A
}
