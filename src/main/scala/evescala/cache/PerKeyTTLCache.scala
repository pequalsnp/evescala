package evescala.cache

import com.twitter.storehaus.cache.TTLCache
import com.twitter.util.Duration

class PerKeyTTLCache[K, V](
  defaultTTL: Duration
) extends TTLCache[K, V](ttl = defaultTTL, cache = Map.empty)(() => System.currentTimeMillis) {
  def putWithTTL(kv: (K, V), ttl: Duration): (Set[K], TTLCache[K, V]) = {
    val (k, v) = kv
    val now: Long = clock()
    putWithTime((k, (now + ttl.inMilliseconds, v)), now)
  }
}
