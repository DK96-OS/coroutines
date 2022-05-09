# Coroutines

This Repository is here to provide __Coroutine__ powered data processing structures.

## Coroutine Queue
The first data processing structure to be designed and created. It allows a set number of 
Coroutines to be launched asynchronously, while maintaining a record of the order that the 
coroutines were added to the Queue.


The benefit of this is that the output of a set of coroutines can be structured, and ordered 
predictably.


The Queue can perform a transformation on the items in a List asynchronously using the 
`transformList` method.