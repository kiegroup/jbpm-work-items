Remote task handler
===================


Feature set
===========
- Simple process definition (one box per operation)
- Tailored for long-running tasks
    - Http request, http callback
- Internally handled (graceful) timeout
- Internally handled (graceful) cancel
- Internal request retry 
- Completion status conditional execution (success/failed/timeout/cancel) 


Task Definition
===============

In a business process use execute-rest sub-process to invoke a remote task and wait for completion. 

- Request (request to start remote task)
  - requestUrl*
  - requestMethod*
  - requestHeaders
  - requestTemplate (request body)
  - maxRetries (default 0 - disabled)
  - retryDelay (default 0)
- Cancel (request to cancel remote task)
  - cancelUrlJsonPointer (json path to extract cancel url form the invocation response)
  - cancelUrlTemplate (alternative to cancelUrlJsonPointer)
  - cancelMethod
  - cancelHeaders
  - cancelTimeout (time to wait for a graceful cancel)
  - ignoreCancelSignals (task run also when cancel has been requested)
- successEvalTemplate* (boolean condition to determine task completion status)
- noCallback (task result is send as invocation response, default: false)
- taskTimeout* (time to wait for remote result before triggering cancel)
- heartbeatTimeout (mark service as DIED when there is no heart-beat, disabled if not set)


Request template
================
Is a message body sent when invoking remote task.
In the template you can use process input parameters, values from other task results and predefined _system_ properties.
For an example see `org.jbpm.contrib.bpm.TestFunctions.getPreBuildTemplate()`.

**System properties**
- system.callbackUrl
- system.callbackMethod
- system.heartBeatUrl
- system.heartBeatMethod

**Including whole object map**
Note that all values are stored as nested maps, 
to include the whole object in the template you have to serialize it and to unescape it, to prevet double json escape.
Use `org.jbpm.contrib.restservice.util.Mapper().writeValueAsString(object, true)` to unescape the serialized object.
See `org.jbpm.contrib.bpm.TestFunctions.getCompletionTemplate()`.


Task Result
===========
Based on the result of a `successEvalTemplate` the task exits normally or exceptionally.
In both cases a `result` Map<String, Object> is available, with keys:
- status
  - SUCCESS
  - FAILED (when successEvalTemplate returns false)
  - CANCELLED
  - TIMED_OUT
  - DIED
  - SYSTEM_ERROR
- initialResponse (invocation response, de-serialized as Map<String, Object>)
- callbackResponse (de-serialized as Map<String, Object>)
- response (callbackResponse or initialResponse, depends on noCallback parameter)
- cancelResponse (response of the cancel invocation, de-serialized as Map<String, Object>)
- error

Result example:
```
{
    status=SUCCESS,
    response={
        scm={
            url=https://github.com/kiegroup/jbpm-work-items.git, 
            revision=new-scm-tag
        }, 
        status=SUCCESS
    }, 
    callbackResponse={
        scm={
            url=https://github.com/kiegroup/jbpm-work-items.git, 
            revision=new-scm-tag
        }, 
        status=SUCCESS
    }, 
    initialResponse={
        cancelUrl=http://localhost:8080/demo-service/service/cancel/0?delay=1
    }, 
    cancelResponse=null, 
    error=null
}
```
- note it's a nested map not a json
- there are multiple `status` fields, the top-level one is the result of the "process" task, 
  while the one under the response is the result which has been returned from the remote service and coincidently has the same name
- we see that the `response` is the same as `callbackResponse`, that's because `noCallback` has not been set


Task invocation details
=======================
The execute-rest sub-process is used to start the remote task and wait for its completion either by a http response or an async callback.

If the task invocation fail due to an error and the `maxRetries` is greater than 0 the invocation is re-tried. 
The invocation retry is delayed for `retryDelay * retry-attmpt` millis. 

When the operation cancel request is received, the remote task is tried to be gracefully cancelled by invoking the cancelUrl which is defined by `cancelUrlJsonPointer` or `cancelUrlTemplate`.
If the `cancelTimeout` is reached, the graceful cancel attempt is ignored. The task completes with status "CANCELLED" regardless of the success of graceful cancel.

If the `taskTimeout` is reached, the cancel procedure is triggered. The task completes with status "TIMED_OUT".

When the process is waiting for a callback, there is an active internal heart-beat monitor (enabled by setting a `heartbeatTimeout`), 
if the time-out is reached, the task completes with status "DIED".

When everything goes well the final status "SUCCESS" or "FAILED" is determined based on boolean condition defined in `successEvalTemplate`.
Sample temaplte: `@{status=="SUCCESS"}`, where a `status` field is a field in the result's response (from a remote task).

## Error handling

execute-rest sub-process error exit conditions:
- `successEvalTemplate` evaluates to `false`
- `taskTimeout` is reached
- `heartbeatTimeout` is reached
- cancel requested
- communication error with remote service

When any of the error exit conditions happen the error completion event can be caught with an error catching boundary event of type `operationFailed`. 

## Heart-beat

Hearth-beat monitor is integrated in the execute-rest sub-process. It is enabled by setting the `heartbeatTimeout` variable.
When enabled a remote task have to periodically notify the process that the task is still running.
The url to send the beat is provided by the system variables `system.heartBeatUrl` and `system.heartBeatMethod` that can be used in the remote task invocation template.


Setting up
==========
Upload this work item handler to your JBPM server and import the `execute-rest.bpmn` (src/test/resources/execute-rest.bpmn) process.

To design your task invocation process use the execute-rest as a sub-process.

See a usage example in `rest-service-workitem/src/test/resources/test-process.bpmn` and `org.jbpm.contrib.bpm.TestFunctions`.
