package forex

package object cache {

  type CacheStorage[F[_]] = rates.Algebra[F]
  final val CacheStorage = rates.Interpreters

}