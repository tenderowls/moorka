package moorka.rx.base.bindings

import moorka.rx.base.{Rx, Source}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[rx] class StatefulBinding[From, To](initialValue: Option[From],
                                            parent: Source[From],
                                            lambda: From ⇒ Rx[To])
  extends Binding[From, To](parent, lambda) {

  var state: Option[To] = None

  override private[rx] def update(v: To): Unit = {
    state = Some(v)
    super.update(v)
  }

  override def flatMap[B](f: (To) => Rx[B]): Rx[B] = {
    new StatefulBinding(state, this, f)
  }

  //override def subscribe[U](f: (To) => U): Rx[Unit] = drop(1).foreach(f)

  initialValue foreach run
}
