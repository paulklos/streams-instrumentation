package com.github.paulklos.streamsinstrumentation

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.Function

@Component
class DemoProcess : Function<KStream<String, String>, KStream<String, String>> {

    override fun apply(input: KStream<String, String>): KStream<String, String> = run {
        input
            .peek { key, value ->
                LOGGER.info("Received '$key' -> '$value'")
            }
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            .aggregate(
                { "" },
                { key, new, agg ->
                    val result = if (agg.isBlank()) new else "$agg,$new"
                    LOGGER.info("Aggregating for key '$key': add '$new' into '$agg' results in $result")
                    result
                },
                Materialized
                    .`as`<String, String, KeyValueStore<Bytes, ByteArray>>("demo-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.String())
            )
            .toStream()
            .peek { key, value ->
                LOGGER.info("Returning '$key' -> '$value'")
            }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DemoProcess::class.java)
    }
}
