package net.redheademile.restapi;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RestAPI {

    private final int port;
    private final boolean ssl;
    private ServerSocket serverSocket;
    private Thread serverThread;

    private final List<Consumer<ReceiveRequestEvent>> eventHandlers;

    public RestAPI(int port) {
        this(port, false);
    }

    /**
     * WIP
     * @param port
     * @param ssl
     */
    public RestAPI(int port, boolean ssl) {
        this.port = port;
        this.ssl = ssl;
        this.eventHandlers = new ArrayList<>();
    }

    public void start() throws IllegalArgumentException, SecurityException, IOException {
        if (this.serverSocket != null || serverThread != null)
            throw new IllegalStateException("Already running");

        serverSocket = new ServerSocket(port);
        serverThread = new Thread(() -> {
            while (this.serverThread != null && this.serverThread.isAlive() && this.serverSocket != null) {
                try {
                    Socket connectionSocket = serverSocket.accept();
                    Thread connectionThread = new Thread(new RestClient(connectionSocket, RestAPI.this), connectionSocket.getInetAddress().toString());
                    connectionThread.start();
                }
                catch (SocketTimeoutException e) {
                    System.out.println("Client timed out.");
                }
                catch (IOException e) {
                    if (!(this.serverThread != null && this.serverThread.isAlive() && this.serverSocket != null))
                        break;
                    e.printStackTrace();
                }
            }
        }, "restapi_server");
        this.serverThread.start();
    }

    public void stop() {
        try {
            this.serverSocket.close();
            this.serverThread.interrupt();
            this.serverSocket = null;
            this.serverThread = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerEventHandler(Consumer<ReceiveRequestEvent> eventHandler) {
        this.eventHandlers.add(eventHandler);
    }

    protected void call(ReceiveRequestEvent e) {
        this.eventHandlers.forEach(handler -> {
            try {
                handler.accept(e);
            }
            catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    @Getter
    public class ReceiveRequestEvent {
        private final Map<String, String> headersIn, params, headersOut;

        private final String methodIn, protocolIn, uriIn, contentIn;
        @Setter
        private String protocolOut, reasonPhrase, contentOut;
        @Setter
        private int statusCode;

        protected ReceiveRequestEvent(Map<String, String> headersIn, Map<String, String> params, String methodIn, String protocolIn, String uriIn, String protocolOut, String reasonPhrase, int statusCode, String contentOut, String contentIn) {
            this.headersIn = headersIn;
            this.params = params;
            this.headersOut = new HashMap<>();
            this.methodIn = methodIn;
            this.protocolIn = protocolIn;
            this.uriIn = uriIn;
            this.protocolOut = protocolOut;
            this.reasonPhrase = reasonPhrase;
            this.statusCode = statusCode;
            this.contentOut = contentOut;
            this.contentIn = contentIn;
        }
    }
}
