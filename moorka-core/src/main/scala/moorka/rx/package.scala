 package moorka

import moorka.rx.base.ops.{RxStateOps, RxStreamOps}
import scala.language.implicitConversions

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object rx {

  val Emitter = base.Emitter
  val Bind = binding.BindingMacro
  val Var = base.Var

  type Emitter[A] = base.Emitter[A]
  type RxStream[A] = base.RxStream[A]
  type RxState[A] = base.RxState[A]
  type Var[A] = base.Var[A]

  val Collection = collection.Collection
  type Collection[A] = collection.Collection[A]
  type CollectionView[A] = collection.CollectionView[A]
  
  trait Mortal {
    def kill(): Unit
  }

  implicit def ToRxStreamOps[A](x: RxStream[A]): RxStreamOps[A] = new RxStreamOps(x)
  implicit def ToRxStateOps[A](x: RxState[A]): RxStateOps[A] = new RxStateOps(x)
}
