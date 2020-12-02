# Subscription Protocols

## Status

Proposed


## Context

We want users to be able to feed their updates into other applications, or to do something as events happen in an app.

There's two types of consumers, third-party applications and live clients. 

Existing third-party applications have supported communication interfaces that we can choose from for compatiblity. 
Live clients communication interfaces are determined by us.

We want these these updates to be at near-live speed.

We need a protocol for transmitting these events.


## Decision

### We'll write a subscription service. It will run on a public server.

### For third-party applications we decided to use webhooks, while live clients are supported via websockets.

We've identified three candidate communication protocols: webhooks, websockets, and server sent events.

Webhooks are ubiquitous among third party applications but require a server to be received.
Websockets are fast but not universally supported, and we have to handle fallbacks to other to support older clients. Server sent events have better client support but do not handle two way communication.

### Both consumers can subscribe to events, and use queries.


## Consequences

What becomes easier or more difficult to do because of this change?

By using a subscription server, we have a push API to maintain in addition to REST pull API.

If we're doing resthooks along with websockets, now we have three different servers to maintain for the API and that can be onerous. One of the REST hooks, one for the REST API, and one for the websockets.

