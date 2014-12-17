package moorka.rx.base

import moorka.rx.Mortal
import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait RxStream[A] extends Mortal {

  def linkChild(child: RxStream[A]): Unit

  def unlinkChild(child: RxStream[A]): Unit

  def emit(x: A): Unit
}
