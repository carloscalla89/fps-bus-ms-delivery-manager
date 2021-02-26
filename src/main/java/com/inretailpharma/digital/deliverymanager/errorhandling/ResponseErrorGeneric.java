package com.inretailpharma.digital.deliverymanager.errorhandling;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Data
public class ResponseErrorGeneric<E> {

    private E body;

    public Mono<E> getErrorFromClientResponse(ClientResponse clientResponse) {
        return clientResponse.body(BodyExtractors.toDataBuffers()).reduce(DataBuffer::write).map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            return bytes;
        })
                .defaultIfEmpty(new byte[0])
                .flatMap(bodyBytes -> Mono.error(new CustomException(clientResponse.statusCode().value()
                        +":"+clientResponse.statusCode().getReasonPhrase()+":"+new String(bodyBytes),
                        clientResponse.statusCode().value()))
                );
    }

}
