package com.tenderowls.moorka

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object core {

  val Emitter = events.Emitter
  type Emitter[A] = events.Emitter[A]
  type Event[A] = events.Event[A]
  type Slot[A] = events.Event[A]

  val Bind = binding.BindingMacro
  val Var = binding.Var

  type Var[A] = binding.Var[A]
  type Bindable[A] = binding.Bindable[A]

  val Collection = collection.Collection
  type Collection[A] = collection.Collection[A]
  type CollectionView[A] = collection.CollectionView[A]
  
  trait Mortal {
    def kill(): Unit
  }
}
