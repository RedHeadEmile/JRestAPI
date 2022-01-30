package net.redheademile.restapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RestClient implements Runnable {

    private final Socket socket;
    private final RestAPI restapi;

    public RestClient(Socket socket, RestAPI restAPI) {
        this.socket = socket;
        this.restapi = restAPI;
    }

    @Override
    public void run() {
        try {
            String method, resource, protocol;
            Map<String, String> headers = new HashMap<>(), params = new HashMap<>();

            // ----- READ -----
            BufferedReader connectionReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            String[] request = connectionReader.readLine().split(" ");
            method = request[0];
            resource = request[1];
            protocol = request[2];

            String header = connectionReader.readLine();
            do {
                if (header == null || header.isEmpty())
                    continue;

                int idx = header.indexOf(":");
                headers.put(header.substring(0, idx).trim(), header.substring(idx + 1).trim());
                header = connectionReader.readLine();
            }
            while (header != null && !header.isEmpty());

            StringBuilder paramsBuilder = new StringBuilder();
            while (connectionReader.ready())
                paramsBuilder.append((char) connectionReader.read());

            for (String param : paramsBuilder.toString().split("&")) {
                int idx = param.indexOf("=");
                if (idx == -1) {
                    continue;
                }
                params.put(URLDecoder.decode(param.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(param.substring(idx + 1), StandardCharsets.UTF_8));
            }

            // ----------------

            RestAPI.ReceiveRequestEvent event = this.restapi.new ReceiveRequestEvent(headers, params, method, protocol, resource, protocol, "OK", 200, "", paramsBuilder.toString());
            this.restapi.call(event);

            // ----- WRITE -----
            final DataOutputStream outStream = new DataOutputStream(this.socket.getOutputStream());
            outStream.writeBytes(event.getProtocolOut()+ " " + event.getStatusCode() + " " + event.getReasonPhrase() + "\r\n");
            event.getHeadersOut().forEach((key, value) -> {
                try {
                    outStream.writeBytes(key + ": " + value + "\r\n");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outStream.writeBytes("\r\n");
            outStream.writeBytes(event.getContentOut());
            outStream.close();
            // -----------------

            this.socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
