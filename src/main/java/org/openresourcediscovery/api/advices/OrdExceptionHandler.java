package org.openresourcediscovery.api.advices;

import static org.springframework.http.CacheControl.noCache;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.notFound;

import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.openresourcediscovery.api.controllers.OpenResourceDiscoveryController;
import org.openresourcediscovery.api.controllers.WellKnownController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {WellKnownController.class, OpenResourceDiscoveryController.class})
public class OrdExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Void> onNoSuchElementException(NoSuchElementException exception) {
    log.warn("Requested resource not found: {}", exception.getMessage());

    return notFound().cacheControl(noCache().mustRevalidate()).build();
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Void> onException(Exception exception) {
    log.error("Unexpected error occurred", exception);

    return internalServerError().cacheControl(noCache().mustRevalidate()).build();
  }
}
