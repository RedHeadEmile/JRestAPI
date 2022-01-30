import net.redheademile.restapi.RestAPI;

import java.io.IOException;
import java.util.Map;

public class RestAPIExample {
    public static void main(String[] args) {
        RestAPI rest = new RestAPI(8080, true);

        rest.registerEventHandler(e -> {
            e.setStatusCode(200);
            e.setReasonPhrase("OK");

            if (e.getUriIn().startsWith("/deploy/")) {
                String auth = e.getHeadersIn().get("Authentication");
                if (auth != null && auth.equals("myreallysecrettoken")) {
                    //Runtime.getRuntime().exec("bash " + e.getUriIn().substring(8));
                    System.out.println("bash " + e.getUriIn().substring(8));
                }
                else
                    e.setContentOut("Bad token");
            }
        });

        try {
            rest.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
