package com.tenderowls.moorka.core.collection

import com.tenderowls.moorka.core.Bindable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[collection] class FilteredWithReactiveFilter[A](parent: CollectionView[A],
                                                        f: Bindable[(A) => Boolean])
  extends Filtered[A](parent, f()) {

  val fObserver = f observe { f =>
    filterFunction = f()
    for (e <- origBuffer) {
      refreshElement(e)
    }
  }

  override def kill(): Unit = {
    fObserver.kill()
    super.kill()
  }
}
