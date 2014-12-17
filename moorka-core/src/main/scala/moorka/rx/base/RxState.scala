package moorka.rx.base

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait RxState[A] extends RxStream[A] {

  def apply(): A
}
