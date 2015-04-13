package vaska

import scala.concurrent.Future
import scala.language.dynamics

/**
 * JavaScript Object presentation. 
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait JSObj extends JSLink {

  def get[A](name: String): Future[A] = {
    jsAccess.request("get", this, name)
  }

  def set[A](name: String, value: A): Future[Unit] = {
    jsAccess.request("set", this,  name, value)
  }
  
  def call[A](name: String, args: Any*): Future[A] = {
    val req = Seq("call", this, name) ++ args
    jsAccess.request(req:_*)
  }
}
