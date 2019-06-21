package com.phylage.scaladia.container

import com.phylage.scaladia.container.indexer.Indexer
import com.phylage.scaladia.injector.scope.InjectableScope
import com.phylage.scaladia.provider.Accessor

import scala.reflect.runtime.universe._

private[scaladia] trait Container {
  /**
    * Cache in the injection container.
    *
    * @param value injection object
    * @tparam T injection type
    * @return
    */
  private[scaladia] def cache[T](value: InjectableScope[T]): InjectableScope[T]

  /**
    * May return an injectable object.
    *
    * @return
    */
  private[scaladia] def find[T: WeakTypeTag](requestFrom: Accessor[_]): Option[T]

  /**
    * Generate an indexer.
    *
    * @param x        Injectable object.
    * @param priority priority
    * @tparam T injection type
    * @return
    */
  private[scaladia] def createIndexer[T: WeakTypeTag](x: T, priority: Int): Indexer[T]
}
