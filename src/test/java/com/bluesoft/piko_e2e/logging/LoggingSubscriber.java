package com.bluesoft.piko_e2e.logging;

import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class LoggingSubscriber implements Flow.Subscriber<ByteBuffer> {

    private final Logger log;
    private final StringBuilder sink;
    private final List<ByteBuffer> buffers = new ArrayList<>();

    public LoggingSubscriber(Logger log, StringBuilder sink) {
        this.log = log;
        this.sink = sink;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ByteBuffer item) {
        buffers.add(item);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {
        sink.append("Body: \n");
        for (ByteBuffer buffer : buffers) {
            sink.append(new String(buffer.array()));
        }
        log.info(sink.toString());
    }
}
