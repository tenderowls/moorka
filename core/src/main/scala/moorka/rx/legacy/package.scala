package moorka.rx

import moorka.rx._
import moorka.rx.base.Source
import moorka.rx.base.bindings.StatefulBinding

/**
 * State/Chanel API from moorka < 0.4.0 
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object legacy {

  class RxOperationException(message: String) extends Exception(message)

  implicit final class RxLegacyOps[A](val self: Rx[A]) extends AnyVal {

    def subscribe(f: A ⇒ Unit)(implicit reaper: Reaper = Reaper.nice): Rx[Unit] = {
      self match {
        case x: Var[A] ⇒
          val us = x.drop(1).foreach(f).mark()
          x.addUpstream(us)
          us
        case x: StatefulBinding[_, A] ⇒
          val us = x.drop(1).foreach(f).mark()
          x.addUpstream(us)
          us
        case x: Source[A] ⇒
          val us = x.foreach(f).mark()
          x.addUpstream(us)
          us
        case _ ⇒ throw new RxOperationException("This Rx is not source")
      }
    }

    def listen(f: ⇒ Unit)(implicit reaper: Reaper = Reaper.nice) = {
      subscribe(_ ⇒ f)  
    }
    
    def observe(f: ⇒ Unit)(implicit reaper: Reaper = Reaper.nice) = {
      self match {
        case x: Source[A] ⇒
          val us = x.foreach(_ ⇒ f).mark()
          x.addUpstream(us)
          us
        case _ ⇒ throw new RxOperationException("This Rx is not source")
      }
    }

    def apply(): A = {
      self match {
        case x: Var[A] ⇒ x.x
        case x: StatefulBinding[_, A] ⇒ x.state.get
        case _ ⇒ throw new RxOperationException("This Rx has no state")
      }
    }

    def emit(v: A): Unit = {
      self match {
        case x: Source[A] ⇒ x.update(v)
        case _ ⇒ throw new RxOperationException("This Rx is not source")
      }
    }
  }

  implicit final class VarLegacyOps[A](val self: Var[A]) extends AnyVal {
    def apply(): A = self.x
    def update(v: A) = self.update(v)
  }
}
