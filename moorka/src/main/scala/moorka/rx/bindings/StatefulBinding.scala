package moorka.rx.bindings

import moorka.rx.{Dummy, StatefulSource, Rx, Source}
import moorka.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[moorka] class StatefulBinding[From, To](initialValue: Option[From],
                                            parent: Source[From],
                                            lambda: From ⇒ Rx[To])
  extends StatelessBinding[From, To](parent, lambda)
  with StatefulSource[To] {

  var state: Option[To] = None

  override private[moorka] def update(v: To, silent: Boolean = false): Unit = {
    val optionV = Some(v)
    if (isAlive && state != optionV && !silent) {
      state = optionV
      super.update(v, silent)
    }
  }

  override def flatMap[B](f: (To) => Rx[B])(implicit reaper: Reaper = Reaper.nice): Rx[B] = {
    if (isAlive) {
      reaper.mark(new StatefulBinding(state, this, f))
    }
    else {
      state.fold[Rx[B]](Dummy)(f)
    }
  }

  override def once[U](f: (To) => U)(implicit reaper: Reaper): Rx[Unit] = {
    if (isAlive) {
      val binding = new OnceBinding(this, f)
      reaper.mark(binding)
      state.foreach(binding.run)
      binding
    } else {
      state match {
        case Some(x) ⇒ 
          f(x)
          Dummy
        case None ⇒ Dummy
      }
    } 
  }
  
  initialValue foreach run
}
