# streams-instrumentation
Demo app to illustrate an issue with OpenTelemetry.

The logging is set to JSON output, which unfortunately makes it hard to read in the console, but it's what we use in our projects, and it automatically logs traces and spans this way.

The example is somewhat involved. I haven't had time to create an automated test to demonstrate the point.

# Steps to reproduce

## Start the Kafka components
The `docker-compose.yml` provides all the necessary Kafka components, and a few more. Simply run

    $ docker-compose up -d

## Start the application
Simply run

    $ ./gradlew bootRun

## Now produce some data
In a new terminal, run

    $ docker container exec -it broker /bin/bash

This will open a shell in the broker container, where we can use the Kafka console producer. Start it by running

    [appuser@broker ~]$ kafka-console-producer \
        --bootstrap-server localhost:9092 \
        --topic demo.in \
        --property parse.key=true \ 
        --property key.separator=":"

Now we can publish messages by simply typing `$key:$value`<enter>. For example

    >key:a
    >key:b
    >key:c
    >key:d

The application simply concatenates the values for a key, so this sequence would result in `a, b, c, d`.

## Check the results
For each message received, the application logs three messages, one upon reception, one just before the aggregation happens, and one displaying the result.

For example, the logging for the `key:d` message look like

```json
{
  "@timestamp": "2022-09-09T16:39:34.401+02:00",
  "@version": "1",
  "message": "Received 'key' -> 'd'",
  "logger_name": "com.github.paulklos.streamsinstrumentation.DemoProcess",
  "thread_name": "demo-057c3eff-a7a4-4734-8cdb-c239e94d76ad-StreamThread-1",
  "level": "INFO",
  "level_value": 20000,
  "trace_id": "f8e3131fde80ccbca53cc6303c55cd84",
  "trace_flags": "01",
  "span_id": "88cd1eba29bfd6f2"
}
```

```json
{
  "@timestamp": "2022-09-09T16:39:34.401+02:00",
  "@version": "1",
  "message": "Aggregating for key 'key': add 'd' into 'a,b,c' results in a,b,c,d",
  "logger_name": "com.github.paulklos.streamsinstrumentation.DemoProcess",
  "thread_name": "demo-057c3eff-a7a4-4734-8cdb-c239e94d76ad-StreamThread-1",
  "level": "INFO",
  "level_value": 20000,
  "trace_id": "f8e3131fde80ccbca53cc6303c55cd84",
  "trace_flags": "01",
  "span_id": "88cd1eba29bfd6f2"
}
```

```json
{
  "@timestamp": "2022-09-09T16:39:55.385+02:00",
  "@version": "1",
  "message": "Returning 'key' -> 'a,b,c,d'",
  "logger_name": "com.github.paulklos.streamsinstrumentation.DemoProcess",
  "thread_name": "demo-057c3eff-a7a4-4734-8cdb-c239e94d76ad-StreamThread-1",
  "level": "INFO",
  "level_value": 20000
}
```

## Conclusion
After the aggregation has been done, the trace and span id are gone, and not logged anymore.
