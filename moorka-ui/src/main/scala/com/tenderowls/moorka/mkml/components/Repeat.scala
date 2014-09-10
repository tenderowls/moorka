package com.tenderowls.moorka.mkml.components

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.components.Repeat.ItemRenderer
import com.tenderowls.moorka.mkml.dom._
import org.scalajs.dom

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Repeat {

  type ItemRenderer[A] = (A) => ElementBase

  def apply[A](dataProvider: CollectionView[A], itemRenderer: (A) => ElementBase) = {
    new Repeat[A](Var(dataProvider), itemRenderer)
  }

  def apply[A](dataProvider: Bindable[CollectionView[A]], itemRenderer: ItemRenderer[A]) = {
    new Repeat[A](dataProvider, itemRenderer)
  }
}

class Repeat[A](val dataProvider: Bindable[CollectionView[A]],
                val itemRenderer: ItemRenderer[A])

  extends ElementBase with MKML {

  case class Child(data:A, dom:ElementBase)

  private val displayState = mutable.HashMap[Child, Boolean]()

  private val observers = mutable.HashMap[Child, Event[_]]()

  private var _viewFilter = (_: A) => true

  private var _rxExtractor: (A) => Bindable[_] = null

  private var _dataProvider = dataProvider()

  private var children: CollectionView[Child] = null

  val nativeElement: dom.Element = dom.document.createElement("div")

  dataProvider observe { _ =>
    updateDataProvider()
  }

  private def updateDisplayStateOfChild(child: Child) = {
    if (_viewFilter(child.data)) {
      if (!displayState(child)) {
        RenderContext.appendOperation(
          DomOperation.CustomOperation(
            () => child.dom.nativeElement.classList.remove("hidden")
          )
        )
        displayState(child) = true
      }
    }
    else {
      if (displayState(child)) {
        RenderContext.appendOperation(
          DomOperation.CustomOperation(
            () => child.dom.nativeElement.classList.add("hidden")
          )
        )
        displayState(child) = false
      }
    }
  }

  private def createObserver(child: Child) = {
    if (_rxExtractor != null) {
      observers(child) = _rxExtractor(child.data) subscribe { _ =>
        updateDisplayStateOfChild(child)
      }
    }
  }

  private def killObserver(child: Child) = {
    if (_rxExtractor != null) {
      observers(child).kill()
    }
  }

  private def updateDataProvider() = {

    kill()
    _dataProvider = dataProvider()

    children = _dataProvider.map { x =>
      val child = Child(x, itemRenderer(x))
      displayState(child) = true
      updateDisplayStateOfChild(child)
      createObserver(child)
      child
    }

    RenderContext.appendOperation(
      DomOperation.AppendChildren(nativeElement, children.asSeq.map(_.dom.nativeElement))
    )

    children.added subscribe { x =>
      RenderContext.appendOperation(
        DomOperation.AppendChild(nativeElement, x.dom.nativeElement)
      )
    }

    children.inserted subscribe { x =>
      x.idx + 1 match {
        case idx if idx < children.length =>
          RenderContext.appendOperation(
            DomOperation.InsertChild(
              to = nativeElement,
              element = x.e.dom.nativeElement,
              ref = children(idx).dom.nativeElement
            )
          )
        case _ =>
          RenderContext.appendOperation(
            DomOperation.AppendChild(nativeElement, x.e.dom.nativeElement)
          )
      }
    }

    children.removed subscribe { x =>
      RenderContext.appendOperation(
        DomOperation.RemoveChild(nativeElement, x.e.dom.nativeElement)
      )
      killObserver(x.e)
      x.e.dom.kill()
    }

    children.updated subscribe { x =>
      val oldChild = children(x.idx)
      oldChild.dom.kill()
      RenderContext.appendOperation(
        DomOperation.ReplaceChild(
          element = nativeElement,
          newChild = x.e.dom.nativeElement,
          oldChild = oldChild.dom.nativeElement
        )
      )
    }
  }

  /**
   * Sets elements invisible when they are not satisfy filter
   */
  def viewFilter(f: (A) => Boolean): Repeat[A] = {
    _viewFilter = f
    children.foreach(updateDisplayStateOfChild)
    this
  }

  def makeDataObservable(f: (A) => Bindable[_]): Repeat[A] = {
    _rxExtractor = f
    children.foreach(createObserver)
    this
  }

  override def kill(): Unit = {
    if (children != null) children.kill()
    observers.values.foreach(_.kill())
  }
}
