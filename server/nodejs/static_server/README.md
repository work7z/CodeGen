# Definitions for static_server

Please refer to this docs before you start using the library.

# rules.json

## $.servers

Multiple Servers Configuration

## $.servers[0].protocol

the type of transportation

```
http, tcp, unix, pipe
```

## $.servers[0].listen

```
1234 tcp://hostname:1234 unix:/path/to/socket.sock pipe:\\.\pipe\PipeName
```

## $.servers[0].public

if received value is Array, then accordingly select the first item. In the future, we will support mutilple directory mode

```
(Array)
```

## $.servers[0].cleanUrls

```json
{
  "cleanUrls": false
}
{
  "cleanUrls": [
    "/app/**",
    "/!components/**"
  ]
}
```
