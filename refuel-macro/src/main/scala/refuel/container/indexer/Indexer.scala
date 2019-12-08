package refuel.container.indexer

import refuel.injector.scope.IndexedSymbol

import scala.reflect.ClassTag

/**
  * This is an indexer to register new objects in the injection container.
  *
  * @tparam T
  */
trait Indexer[T] {
  me =>

  /**
    * Index a new symbol in the injection container.
    *
    * @return
    */
  def indexing(): IndexedSymbol[T]

  /**
    * Create a new authorization class for this indexer.
    * If you do this, you will not be able to register the authorization instance.
    *
    * @tparam X Accept class
    * @return
    */
  def accept[X: ClassTag]: Indexer[T]

  /**
    * Register a new authorization instance with this indexer.
    * If you do this, you will not be able to register the authorization class.
    *
    * @param x Accept instance
    * @tparam X Accept instance type
    * @return
    */
  def accept[X](x: X): Indexer[T]

  implicit def self: Indexer[T] = me
}