package vaska

import scala.concurrent.{ExecutionContext, Future, Promise}

object JSAccess {

  final class JSSideException(message: String) extends Exception(message)
  
  val LinkPrefix = "@link:"
  val ArrayPrefix = "@arr:"
  val ObjPrefix = "@obj:"
  val UnitResult = "@unit"
}

/**
 * Provide access to remote page with JavaScript engine 
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait JSAccess { self ⇒

  import JSAccess._

  implicit val executionContext: ExecutionContext

  /**
   * Increment it after request 
   */
  protected var lastReqId = 0

  /**
   * List of promises of requests. Resolves by
   * income messages 
   */
  protected var promises = Map.empty[Int, Promise[Any]]

  /**
   * Abstract method sends message to remote page 
   */
  def send(args: Seq[Any]): Unit

  /**
   * Make request to remote page.
   *
   * @tparam A Type you need
   *
   * @return Future with result.
   *         It may be any basic type or [[JSLink]]
   */
  def request[A](args: Any*): Future[A] = {
    val promise = Promise[Any]()
    val requestId = lastReqId
    val pair = (requestId, promise)
    lastReqId += 1
    promises += pair
    send {
      // Pack arguments
      packArgs(Seq(requestId) ++ args)
    }
    // Unpack result
    promise.future map unpackArg[A]
  }

  def array(linkId: String): JSArray = {
    new JSArray() {
      val jsAccess = self
      val id = linkId
    }
  }

  def obj(linkId: String): JSObj = {
    new JSObj() {
      val jsAccess = self
      val id = linkId
    }
  }

  def platformDependentPack(value: Any) = value

  def platformDependentUnpack(value: Any) = value

  def packArgs(args: Seq[Any]) = args collect {
    case anyLink: JSLink ⇒ LinkPrefix + anyLink.id
    case hook: Hook ⇒ hook.requestString
    case otherwise ⇒ platformDependentPack(otherwise)
  }

  def unpackArg[A](arg: Any) = arg match {
    case s: String if s.startsWith(ObjPrefix) ⇒
      val id = s.stripPrefix(ObjPrefix)
      obj(id).asInstanceOf[A]
    case s: String if s.startsWith(ArrayPrefix) ⇒
      val id = s.stripPrefix(ArrayPrefix)
      array(id).asInstanceOf[A]
    case `UnitResult` ⇒
      ().asInstanceOf[A]
    case otherwise ⇒
      platformDependentUnpack(otherwise).
        asInstanceOf[A]
  }

  def resolvePromise(reqId: Int, isSuccess: Boolean, res: Any) = {
    promises.get(reqId) match {
      case Some(promise) ⇒
        if (isSuccess) {
          promise.success(res)
        }
        else {
          val message = res.asInstanceOf[String]
          val exception = new JSSideException(message)
          promise.failure(exception)
        }
        promises -= reqId
      case None ⇒
    }
  }
}