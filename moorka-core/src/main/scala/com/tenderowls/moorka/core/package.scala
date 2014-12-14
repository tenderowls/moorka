package com.tenderowls.moorka

import com.tenderowls.moorka.core.rx.ops.{RxStateOps, RxStreamOps}
import scala.language.implicitConversions

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object core {

  val Emitter = rx.Emitter
  val Bind = binding.BindingMacro
  val Var = rx.Var

  type Emitter[A] = rx.Emitter[A]
  type RxStream[A] = rx.RxStream[A]
  type RxState[A] = rx.RxState[A]
  type Var[A] = rx.Var[A]

  val Collection = collection.Collection
  type Collection[A] = collection.Collection[A]
  type CollectionView[A] = collection.CollectionView[A]
  
  trait Mortal {
    def kill(): Unit
  }

  implicit def ToRxStreamOps[A](x: RxStream[A]): RxStreamOps[A] = new RxStreamOps(x)
  implicit def ToRxStateOps[A](x: RxState[A]): RxStateOps[A] = new RxStateOps(x)
}
