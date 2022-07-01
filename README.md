# Coroutines

This Repository provides __Coroutine__ powered data processing structures.

## Coroutine Queue
The Queue is the first data structure that maintains an ordered collection of 
Coroutines. It can be constructed with a fixed capacity, or be allowed to hold an
unlimited number of coroutines.

### Queue Methods
Coroutine results can be recovered from the queue using the `awaitNext` or `awaitList` methods.

The `awaitAll` method will discard the Deferred results, but will wait for each coroutine to finish.
It has an optional parameter that limits the number of coroutine results to be discarded.

The `cancel` method will interrupt all running coroutines, and discard everything in the queue.

### Static Queue Methods
The Queue can perform a transformation on the items in a List asynchronously using the 
`transformList` method. Similarly, the Queue can transform an array using the `transformArray` method.

Both of these methods are static, and will construct a fixed size queue to process the input.
