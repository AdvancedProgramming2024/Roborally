package dk.dtu.compute.se.pisd.roborally.Online;

import org.springframework.http.HttpStatusCode;

import java.net.http.HttpResponse;

public class Response<T> {
    protected HttpStatusCode code;
    protected T item;

    public Response(HttpResponse<T> httpResponse) {
        this.code = HttpStatusCode.valueOf(httpResponse.statusCode());
        this.item = httpResponse.body();
    }

    public Response(HttpStatusCode code, T item) {
        this.code = code;
        this.item = item;
    }
    public HttpStatusCode getStatusCode() {
        return code;
    }

    public T getItem() {
        return item;
    }
}
