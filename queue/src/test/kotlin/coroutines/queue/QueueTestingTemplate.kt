package coroutines.queue

import coroutines.examples.InputData
import coroutines.examples.OutputData
import coroutines.examples.TestDataProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/** A Template for [CoroutineQueue] Tests.
 * @author DK96-OS : 2022
 */
abstract class QueueTestingTemplate(
    protected val inputDataSize: Int = 32,
    inputRNG: Long = 300,
) {

    init {
        if (0 > inputDataSize)
            throw IllegalArgumentException()
    }

    /** A Test Data Provider.
     */
    protected val provider = TestDataProvider()

    /** Example Input Data that can be transformed easily.
     */
    protected val input: List<InputData> = provider.createInput(
        inputDataSize, inputRNG
    ).toList()

    /** A CoroutineQueue instance created before each test.
     */
    protected lateinit var queue: CoroutineQueue<OutputData>

    /** Initialize a new CoroutineQueue with the given capacity.
     *  Also, generate an array of InputData with size equal to capacity.
     *  @param capacity Default is 32.
     */
    protected fun init(
        capacity: Int = 32,
    ) {
        queue = CoroutineQueue(capacity)
    }

    /** Create a transformation coroutine for all inputs, add them to Queue.
     *  All transformations will return non-null OutputData.
     */
    protected fun addAllInputData() {
        runBlocking {
            for (i in input) {
                val task = async { i.transform() }
                queue.add(task)
            }
        }
    }

}