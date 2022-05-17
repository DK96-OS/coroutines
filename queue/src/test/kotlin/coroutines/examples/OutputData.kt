package coroutines.examples

/** Data structure for coroutine output
 */
class OutputData(
	val key: Byte,
	val title: String
) {
	/** Reveals when this object was created */
	val createTime: Long = System.nanoTime()
}