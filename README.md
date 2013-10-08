Vert.igo
========

Vertigo is a distributed event processing framework built on the
[Vert.x](http://vertx.io/) application platform. Similar to Storm, work
can be distributed across clusters with a central module assigning work
to Vert.x instances in the cluster.

## Features
* Guaranteed message processing, acking, heartbeats, etc
* Built on the [Vert.x](http://vertx.io/) application platform, so core Vert.x
  APIs, languages, and contributed modules integrate seemlessly with Vertigo
* Easy to learn and integrate with existing applications using the familiar
  Vert.x handler system and Vert.x' native support for JSON
* Allows networks to be deployed locally within a single Vert.x instance or across
  a cluster of Vert.x instances (using [Via](https://github.com/kuujo/via)
* Supports remote-procedure calls, allowing for real-time request-response
  spanning multiple Vert.x instances

## How it works

### Networks
A network is the representation of a collection of components - special Vert.x
verticle implementations- and the connections between them. Put together,
a network processes streams of data in real-time. Vert.igo puts no limitations
on network structures. Each component may be connected to zero or many other components,
and circular relationships may be created as well. Networks are defined using
the `NetworkDefinition` API and deployed using [Via](https://github.com/kuujo/via)
clusters.

![Vert.igo Network](http://s9.postimg.org/xuv3addj3/vertigo_complex_network.png)

This is an example of a complex network. Given a set of Vert.x verticles or
modules, Vertigo uses a code-based representation of the network structure
to define connections between network components, start component verticles
or modules, monitor components, and manage communication between them in a fast,
reliable manner.

### Components
A component represents a single vertex in a Vertigo network graph. Each network
may contain any number of components, and each component may have any number of
instances running within the network (each of which may be assigned to different
machines around a cluster). Within the context of Vert.x a component can be
defined as a verticle that may receive messages from zero or many verticles and
send messages to one or many verticles. What happens within the verticle depends
entirely where they appear in a network graph and how the component is implemented.
Fortunately, Vertigo provides several types of components for common tasks.

#### Feeders
Feeders are components whose sole responsibility is to emit data. Data generated
by feeders may come from any source, and Vertigo provides a number of feeder
implementations for integrating networks with a variety of Vert.x and other APIs.

##### Example
*Java*
```java
BasicFeeder feeder = vertigo.createBasicFeeder();
feeder.start(new Handler<AsyncResult<BasicFeeder>>() {
  public void handle(AsyncResult<BasicFeeder> result) {
    BasicFeeder feeder = result.result();
    feeder.setMaxQueueSize(1000);
    final JsonObject data = new JsonObject().putString("body", "Hello world!");
    feeder.feed(data, new Handler<AsyncResult<Void>>() {
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          // Processing failed. Re-emit the message.
          feeder.feed(data);
        }
        else {
          // Processing succeeded!
        }
      }
    });
  }
});
```

*Python*
```python
feeder = vertigo.create_basic_feeder()

def start_handler(error, feeder):
  if not error:
    def ack_handler(error):
      if not error:
        pass # Message was successfully processed!
    feeder.feed({'body': 'Hello world!'}, ack_handler)

feeder.start(start_handler)
```

#### Workers
Worker components are the primary units of processing in Vertigo. Workers
are designed to both receive input and emit output. The processes that are
performed in between depend entirely on the implementation.

##### Example
*Java*
```java
final Worker worker = vertigo.createWorker();
worker.messageHandler(new Handler<JsonMessage>() {
  public void handle(JsonMessage message) {
    JsonArray words = message.getArray("words").size();
    if (words != null) {
      // Emit the number of words.
      worker.emit(new JsonObject().putNumber("count", words), message);
      worker.ack(message);
    }
    else {
      // This is an invalid message. Fail it.
      worker.fail(message);
    }
  }
}).start();
```

*Python*
```python
worker = vertigo.create_worker()

@worker.message_handler
def message_handler(message):
  if message.body.has_key('words'):
    worker.emit({'count': len(message.body['words'])}, parent=message)
    worker.ack(message)
  else:
    worker.fail(message)
```

#### Executors
Executors are components that execute part or all of a network essential
as a remote procedure invocation. Data emitted from executors is tagged
with a unique ID, and new messages received by the executor are correlated
with past emissions.

### Defining networks
Networks are defined in code using an object-oriented API. Just as network
components can be written in any Vert.x supported language, networks can
be defined using any Vert.x language.

### Messaging
One of the primary purposes of Vertigo is managing communication between
network components in a reliable manner. Of course, Vertigo uses the Vert.x
event bus for inter-component communication, but Vertigo also provides many
reliability features on top of the event bus.

#### How components communicate
Network components communicate with each other directly over the event bus
rather than being routed through a central message broker. When a network
is created, Vertigo assigns unique addresses *to each component (verticle)
instance* which that verticle uses to communicate with the verticles around
it. Thus, each component instance essentially maintains a direct connection
to its neighbors, ensuring fast messaging between them.

![Communication Channels](http://s7.postimg.org/unzwkrvgb/vertigo_channel.png)

#### Dispatchers
With each component instance maintaining its own unique event bus address,
Vertigo needs a way to determine which component messages emitted from one
component are dispatched to. Each component may define a *grouping* which
determines how messages are distributed among multiple instances of the
component. For instance, one component may need to receive all messages
emitted to it, while another may be able to receive messages in a round-robin
fashion. Vertigo provides groupings for various scenarios, including
consistent-hashing based groupings.

#### Filters
Vertigo messages contain metadata in addition to the message body. And just
as with grouping component instances, sometimes components may be only
interested in receiving messages containing specific metadata. For this,
Vertigo provides message filters which allow components to define the types
of messages they receive.

### Message acking
Vertigo's inherent control over the messaging infrastructure of component
verticles allows it to provide some unique reliability features. When a
Vertigo network is deployed, a special verticle called the *auditor* verticle
is deployed along with it. The Vertigo *auditor* is tasked with monitoring
messages within the network, tracking acks and nacks throughout the network,
and notifying *feeders* when a message tree fails.

![Network Auditor](http://s14.postimg.org/kkl297qo1/vertigo_acker.png)

When a *feeder* component creates and emits a new message, the feeder's
`OutputCollector` will notify the network's *auditor* of the newly created
message. *It is the responsibility of each worker component to ack or fail
the message*. If a worker creates a child of the message, the auditor is
told about the new relationship. If a descendant of a message is failed,
the original feeder will be notified of the failure. The feeder may
optionally replay failed messages through the network. Once *all*
descendants of a message have been successfully *acked*, the original
feeder will be notified. The *auditor* maintains a timer for each message
tree (the timers are actually shared among several message trees). If the
timer is triggered prior to the entire message tree being *acked*, the
tree will be failed and the original feeder will be notified.

### Clustering
Vertigo provides APIs for deploying networks both locally (within a single
Vert.x instance) as well as distributing network components across multiple
Vert.x instances using [Via](https://github.com/kuujo/via), a distributed
deployment framework for Vert.x (Via was specifically developed for Vertigo).

### [See the documentation for tutorials and examples](https://github.com/kuujo/vertigo/wiki/Vertigo)
