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
  - requestUrl
  - requestMethod
  - requestHeaders
  - requestTemplate (request body)
- Cancel (request to cancel remote task)
  - cancelUrlJsonPointer (json path to extract cancel url form the invocation response)
  - cancelUrlTemplate (alternative to cancelUrlJsonPointer)
  - cancelMethod
  - cancelHeaders
  - cancelTimeout (time to wait for a graceful cancel)
  - ignoreCancelSignals (task run also when cancel has been requested)
- successEvalTemplate (boolean condition to determine task completion status)
- noCallback (task result is send as invocation response)
- taskTimeout (time to wait for remote result before triggering cancel)
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

