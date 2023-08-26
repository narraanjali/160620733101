import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import org.json.JSONArray;

public class NumberManagementService {

    public static void main(String[] args) {
        int port = 8008; // Port to run the service on
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/numbers", new NumbersHandler());
            server.start();
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class NumbersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, List<String>> queryParams = splitQuery(exchange.getRequestURI().getQuery());
            List<Integer> mergedNumbers = new ArrayList<>();

            for (String url : queryParams.getOrDefault("http://20.244.56.144/numbers/primes", new ArrayList<>())) {
                List<Integer> numbers = fetchNumbersFromUrl(url);
                mergedNumbers.addAll(numbers);
            }

            Collections.sort(mergedNumbers);
            List<Integer> uniqueNumbers = new ArrayList<>(new HashSet<>(mergedNumbers));

            String response = new JSONArray(uniqueNumbers).toString();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static List<Integer> fetchNumbersFromUrl(String urlString) {
        List<Integer> numbers = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500); // Timeout in milliseconds
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    numbers.add(jsonArray.getInt(i));
                }
            }

            connection.disconnect();
        } catch (Exception e) {
            // Handle exceptions, e.g., timeout, invalid URL, etc.
            e.printStackTrace();
        }
        return numbers;
    }

    private static Map<String, List<String>> splitQuery(String query) {
        Map<String, List<String>> queryPairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                String key = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                queryPairs.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return queryPairs;
    }
}
