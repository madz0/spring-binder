package com.github.madz0.springbinder.utils.rest;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RestClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public RestClient(String ip, short port) {
        this(ip, port, false);
    }

    public RestClient(String ip, short port, boolean isSecure) {
        this("http"+(isSecure?"s":"")+"://"+ip+":"+port);
    }

    public RestClient(String baseUrl) {
        this(baseUrl, 5000, 30000);
    }

    public RestClient(String ip, short port, int connectTimeout, int readTimeout) {
        this(ip, port, false, connectTimeout, readTimeout);
    }

    public RestClient(String ip, short port, boolean isSecure, int connectTimeout, int readTimeout) {
        this("http"+(isSecure?"s":"")+"://"+ip+":"+port, connectTimeout, readTimeout);
    }

    public RestClient(String baseUrl, int connectTimeout, int readTimeout) {
        this.baseUrl = baseUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restTemplate = new RestTemplate(factory);
    }

    protected String getPath(String action) {
        return String.format("%s%s", baseUrl, action);
    }

    protected HttpHeaders getHeaders(HttpHeaders additionalHeaders) {
        HttpHeaders headers = getDefaultHeaders();
        if(additionalHeaders == null){
            return headers;
        }
        headers.putAll(additionalHeaders);
        return headers;
    }

    protected HttpEntity getHttpEntity(Object object, HttpHeaders additionalHeaders) {
        return new HttpEntity<>(object, getHeaders(additionalHeaders));
    }

    protected RestTemplate getRestTemplate(){
        return restTemplate;
    }

    protected HttpHeaders getDefaultHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
        return headers;
    }

    // exchange async

    public <T> void doReq(String action, HttpMethod method, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType,
                        Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType,
                        Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, HttpHeaders additionalHeaders,
                        Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Object requestEntity, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Object requestEntity, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Object requestEntity,
                                  HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Object requestEntity,
                                  HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables), callback);
    }

    // exchange sync

    public <T> RestResult<T> doReq(String action, HttpMethod method, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, null), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                method, getHttpEntity(null, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Object requestEntity, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Object requestEntity, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Object requestEntity,
                                  HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Object requestEntity,
                                  HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, null), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> doReq(String action, HttpMethod method, Map<String, ?> uriVariables, Object requestEntity,
                                  HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), method,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables));
    }

    // post async

    public <T> void post(String action, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType), callback);
    }

    public <T> void post(String action, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType), callback);
    }

    public <T> void post(String action, HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType), callback);
    }

    public <T> void post(String action, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType,
                          Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType,
                          Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders,
                          Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Object requestEntity, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType), callback);
    }

    public <T> void post(String action, Object requestEntity, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType), callback);
    }

    public <T> void post(String action, Object requestEntity,
                          HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType), callback);
    }

    public <T> void post(String action, Object requestEntity,
                          HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, Object requestEntity,
                          Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, Object requestEntity,
                          ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, Object requestEntity,
                          HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void post(String action, Map<String, ?> uriVariables, Object requestEntity,
                          HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables), callback);
    }

    // post sync

    public <T> RestResult<T> post(String action, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType));
    }

    public <T> RestResult<T> post(String action, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType));
    }

    public <T> RestResult<T> post(String action, HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType));
    }

    public <T> RestResult<T> post(String action, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, null), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.POST, getHttpEntity(null, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Object requestEntity, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType));
    }

    public <T> RestResult<T> post(String action, Object requestEntity, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType));
    }

    public <T> RestResult<T> post(String action, Object requestEntity,
                                   HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType));
    }

    public <T> RestResult<T> post(String action, Object requestEntity,
                                   HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, Object requestEntity,
                                   Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, Object requestEntity,
                                   ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, null), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, Object requestEntity,
                                   HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> post(String action, Map<String, ?> uriVariables, Object requestEntity,
                                   HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action), HttpMethod.POST,
                getHttpEntity(requestEntity, additionalHeaders), responseType, uriVariables));
    }

    // get async

    public <T> void get(String action, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType), callback);
    }

    public <T> void get(String action, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType), callback);
    }

    public <T> void get(String action, HttpHeaders additionalHeaders, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType), callback);
    }

    public <T> void get(String action, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType,
                          Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType), callback);
    }

    public <T> void get(String action, Map<String, ?> uriVariables, Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType, uriVariables), callback);
    }

    public <T> void get(String action, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType,
                          Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType, uriVariables), callback);
    }

    public <T> void get(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders,
                          Class<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType, uriVariables), callback);
    }

    public <T> void get(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType, Consumer<RestResult<T>> callback){
        doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType, uriVariables), callback);
    }

    // get sync

    public <T> RestResult<T> get(String action, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType));
    }

    public <T> RestResult<T> get(String action, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType));
    }

    public <T> RestResult<T> get(String action, HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType));
    }

    public <T> RestResult<T> get(String action, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType));
    }

    public <T> RestResult<T> get(String action, Map<String, ?> uriVariables, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType, uriVariables));
    }

    public <T> RestResult<T> get(String action, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, null), responseType, uriVariables));
    }

    public <T> RestResult<T> get(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, Class<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType, uriVariables));
    }

    public <T> RestResult<T> get(String action, Map<String, ?> uriVariables, HttpHeaders additionalHeaders, ParameterizedTypeReference<T> responseType){
        return doExchange((restTemplate) -> restTemplate.exchange(getPath(action),
                HttpMethod.GET, getHttpEntity(null, additionalHeaders), responseType, uriVariables));
    }

    // do exchange

    private <T> void doExchange(Function<RestTemplate, ResponseEntity<T>> function, Consumer<RestResult<T>> callback){
        Thread thread = new Thread(() -> callback.accept(doExchange(function)));
        thread.setDaemon(true);
        thread.start();
    }

    private <T> RestResult<T> doExchange(Function<RestTemplate, ResponseEntity<T>> function){
        try{
            ResponseEntity<T> result = function.apply(getRestTemplate());
            if(result.getStatusCode().is2xxSuccessful()){
                return new RestResult<>(result.getBody(), result.getStatusCode(), null);
            }
            return new RestResult<>(null, result.getStatusCode(), null);
        }catch (HttpStatusCodeException e){
            return new RestResult<>(null, e.getStatusCode(), e.getResponseBodyAsString());
        }catch (ResourceAccessException e){
            return new RestResult<>(null, null, e.getMessage());
        }
    }

}
