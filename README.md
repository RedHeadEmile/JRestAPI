# RestAPI

## Example
Here is an example where the server will execute a script if the client pass the right token in the header `Authentication`.
So `http://127.0.0.1:8080/deploy/web` will execute `/home/rest/scripts/web.sh`.
```java
RestAPI rest = new RestAPI(8080);

rest.registerEventHandler(handler -> {
    e.setStatusCode(200);
    e.setReasonPhrase("OK");
    
    if (e.getUriIn().startsWith("/deploy/")) {
        String auth = e.getHeadersIn().get("Authentication");
        if (auth != null && auth.equals("myreallysecrettoken")) {
            try {
                Runtime.getRuntime().exec("/home/rest/scripts/" + e.getUriIn().substring(8) + ".sh");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else
            e.setContentOut("Bad token");
    }
});

try {
    rest.start();
} catch (IOException ex)  {
    ex.printStackTrace();
}
```

Here is all the methods you can use on the handler object:
| Method | Return | Description |
| ------ | ------ | ------ |
| getMethodIn() | String | Get the method of the incoming request |
| getProtocolIn() | String | Get the protocol of the incoming request |
| getUriIn() | String | Get the URI of the incoming request |
| getContentIn() | String | Get the additional content in the incoming request |
| getHeadersIn() | Map<String, String> | Get the headers of the incoming request |
| getParams() | Map<String, String> | Get the params of the incoming request (if it's a POST request) |
| getProtocolOut() | String | Get the outgoing protocol (the same as the incoming protocol by default)
| setProtocolOut(String protocol) | void | Set the outgoing protocol written in the answer |
| getReasonPhrase() | String | Get the reason phrase returned to the client ("OK" by default)
| setReasonPrase(String reasonPhrase) | void | Set the reason phrase written in the answer |
| getStatusCode() | int | Get the status code returned to the client (200 by default)
| setStatusCode(int statusCode) | void | Set the status code return to the client |
| getHeadersOut() | Map<String, String> | Get (and set) the headers of outgoing response |
| getContentOut() | String | Get the page content return to the client (empty by default) |
| setContentOut(String contentOut) | void | Set the content returned to the client |

## Dependencies
Only `lombok`, used to generate the getters and the setters of the handler class (`ReceiveRequestEvent`).