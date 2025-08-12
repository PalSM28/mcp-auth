package com.ninja.api_gateway.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.error("API Gateway Error: ", ex);
        
        var response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";
        
        if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            var responseStatusException = (org.springframework.web.server.ResponseStatusException) ex;
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            message = responseStatusException.getReason();
        }
        
        response.setStatusCode(status);
        
        String errorJson = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\", \"path\": \"%s\"}", 
                status.getReasonPhrase(),
                message,
                java.time.Instant.now(),
                exchange.getRequest().getPath().toString()
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
