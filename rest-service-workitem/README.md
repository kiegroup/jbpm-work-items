(Micro) Service Orchestration with JBPM
=======================================

.footnote[[Matej Lazar](mailto:matejonnet@gmail.com)]

???

Visit [http://matejonnet.github.io/LINK!!] (http://matejonnet.github.io/LINK!!) to see slides in presentation mode.

---
Feature set
===========
- Orchestrated flow (not a choreography)
- Simple process definition (one box per operation)
- Ideal for long running operations
    - Http request, http callback
- Support for result passing
- Internally handled timeout
- Internally handled cancel
- Declarative conditional execution (failed/timeout/cancel) 

---
[![Process definition](images/3_min.png)](images/3.png)
[![Process definition](images/4_min.png)](images/4.png)
[![Task definition](images/5_min.png)](images/5.png)
---

Task Definition
===============
- requestUrl
- requestMethod
- requestBody
- taskTimeout
- cancelUrlJsonPointer
- cancelTimeout
- alwaysRun
- mustRunAfter
- successCondition
---

Service request
===============
- requestUrl, requestMethod
- request body
```json
{
  "name":"Matej",
  "credit":"#{previousServiceResult.credit.amount}",
  "callbackUrl":"${handler.callback.url}"
}
```
- any parameter name can be used instead of `callbackUrl`
- `${handler.callback.url}` will be replaced with required callback url
---

Service response
================
- status of request for an operation
- remote service can use any message to define cancel url
```json
{ "cancelUrl":"http://remote.service/task/10/cancel" }
```
- Task parameter `cancelUrlJsonPointer` defines where to read cancel url from
---

Service result - callback
=========================
- sent to endpoint defined by request
- result is mapped to process variable for use in other invocations
---

Cancel
======
- global cancel support via process signal event `cancel-all:pid`
- cancel invoked on all running services
- internal cancel after `cancelTimeout`
---

Timeout
=======
- per task configured timeout using `taskTimeout` parameter
- on timeout service cancel is invoked
---

Execution control
=================
- alwaysRun (true|false)
    - task is executed regardless of previous results
- mustRunAfter (list,of,taskNames)
    - task is executed if listed task completed successfully
- successCondition
    - MVEL expression to determine successful service completion
        - example:
        - `firstServiceResult.person != empty`
        - `secondServiceResult.status == SUCCESS`

---

Try it
======
- implemented as custom workitem handler
- download JBPM (7.9.0.Final)
- build and deploy rest-service-handler
    - https://github.com/matejonnet/jbpm-work-items/tree/rest-service-handler/rest-service-workitem
---

