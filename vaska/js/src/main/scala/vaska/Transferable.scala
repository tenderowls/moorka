package vaska

/**
 * Wrapper for object should be transferred by worker
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
case class Transferable[+T](value: T)
