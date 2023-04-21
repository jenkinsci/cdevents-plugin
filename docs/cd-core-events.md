> *WORK IN PROGRESS*

## Continuous Delivery Core Events

### Common CDEvent Context Attributes

- `specversion` - The version of the CDEvents specification which the event uses.
- `id` - (REQUIRED) Identifier for an event. Subsequent delivery attempts of the same event MAY share the same id. This
  attribute matches the syntax and semantics of the id attribute of CloudEvents.
- `source` - (REQUIRED) Defines the context in which an event happened. The main purpose of the source is to provide
  global uniqueness for source + id.
- `type` - (REQUIRED) Defines the type of event, as combination of a subject, predicate and version. Valid event types
  are defined in the vocabulary. All event types should be prefixed with dev.cdevents. One occurrence may have multiple
  events associated, as long as they have different event types. Versions are semantic in the major.minor.patch format.
  For more details about versions see the the see versioning documentation.
- `time` - (REQUIRED) Defines the time of the occurrence. When the time of the occurrence is not available, the time
  when the event was produced MAY be used.
- `data` - Contains all the relevant details specific to the event type.

### Pipeline Run

Additional CDEvent Context Attributes

- `pipelinename` - The name of the pipeline.
- `url` - url to the pipelineRun.

#### Examples
---
Pipeline Run: Queued

```
{
  "specversion" : "0.3",
  "id" : "7",
  "source" : "job/PipelineCommit/",
  "type" : "dev.cdevents.pipelinerun.queued.0.1.0",
  "time" : "2023-04-06T16:55:55.5997199-04:00",
  "pipelinename" : "PipelineCommit",
  "url" : "job/PipelineCommit/",
  "data" : {
    "name" : "PipelineCommit",
    "url" : "http://localhost:8080/jenkins/job/PipelineCommit/7/",
    "userId" : null,
    "userName" : null,
    "causes" : [ "Started by user anonymous" ]
  }
}
```

---
Pipeline Run: Started

```json
{
  "specversion" : "0.3",
  "id" : "167",
  "source" : "job/",
  "type" : "dev.cdevents.pipelinerun.started.0.1.0",
  "time" : "2023-04-06T16:55:55.5997199-04:00",
  "pipelinename" : "PipelineCommit",
  "url" : "job/PipelineCommit/167/",
  "data" : {
    "userId" : null,
    "userName" : null,
    "name" : "PipelineCommit",
    "displayName" : "PipelineCommit",
    "url" : "job/PipelineCommit/",
    "build" : {
      "fullUrl" : "http://localhost:8080/jenkins/job/PipelineCommit/167/",
      "number" : 167,
      "queueId" : 7,
      "duration" : 0,
      "status" : null,
      "url" : "job/PipelineCommit/167/",
      "displayName" : null,
      "parameters" : null,
      "scmState" : {
        "url" : null,
        "branch" : null,
        "commit" : null
      }
    }
  }
}
```

---
Pipeline Run: Finished

```
{
  "specversion" : "0.3",
  "id" : "167",
  "source" : "job/",
  "type" : "dev.cdevents.pipelinerun.finished.0.1.0",
  "time" : "2023-04-06T16:56:06.8987104-04:00",
  "pipelinename" : "PipelineCommit",
  "url" : "job/PipelineCommit/167/",
  "outcome" : "success",
  "errors" : "",
  "data" : {
    "userId" : null,
    "userName" : null,
    "name" : "PipelineCommit",
    "displayName" : "PipelineCommit",
    "url" : "job/PipelineCommit/",
    "build" : {
      "fullUrl" : "http://localhost:8080/jenkins/job/PipelineCommit/167/",
      "number" : 167,
      "queueId" : 7,
      "duration" : 12165,
      "status" : "SUCCESS",
      "url" : "job/PipelineCommit/167/",
      "displayName" : null,
      "parameters" : null,
      "scmState" : {
        "url" : null,
        "branch" : null,
        "commit" : null
      }
    }
  }
}
```

### Task Run

#### Started

```
{
  "specversion" : "0.3",
  "id" : "167",
  "source" : "job/",
  "type" : "dev.cdevents.taskrun.started.0.1.0",
  "time" : "2023-04-06T16:56:01.7967096-04:00",
  "taskname" : "PipelineCommit",
  "pipelinerun" : "{\"id\":null,\"source\":null,\"pipelineName\":null,\"outcome\":null,\"url\":null,\"errors\":null}",
  "url" : "job/PipelineCommit/167/",
  "data" : {
    "stageName" : "Allocate node : Start",
    "stageNodeUrl" : "job/PipelineCommit/167/execution/node/3/",
    "arguments" : null,
    "log" : null
  }
}
```

#### Finished

```
{
  "specversion" : "0.3",
  "id" : "167",
  "source" : "job/",
  "type" : "dev.cdevents.taskrun.finished.0.1.0",
  "time" : "2023-04-06T16:56:03.1958579-04:00",
  "taskname" : "PipelineCommit",
  "pipelinerun" : "{\"id\":null,\"source\":null,\"pipelineName\":null,\"outcome\":null,\"url\":null,\"errors\":null}",
  "url" : "job/PipelineCommit/167/",
  "outcome" : "success",
  "errors" : "",
  "data" : {
    "stageName" : "Run arbitrary Pipeline script : Body : End",
    "stageNodeUrl" : "job/PipelineCommit/167/execution/node/9/",
    "arguments" : null,
    "log" : null
  }
}
```