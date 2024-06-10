package dk.dtu.compute.se.pisd.roborally.online;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ResponseCenter<T> {
    public ResponseEntity<T> response(T item) {
        HttpStatusCode statusCode = (item == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return new ResponseEntity<>(item, statusCode);
    }

    public ResponseEntity<T> ok() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<T> notFound() { return new ResponseEntity<>(HttpStatus.NOT_FOUND); }
    public ResponseEntity<String> badRequest(String errorMessage) { return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST); }
}
