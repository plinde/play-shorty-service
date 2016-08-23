package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.params.Parameters;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result lookup() throws IOException {

        JsonNode json = request().body().asJson();
        String hash = json.findPath("hash").textValue();
        System.out.println(hash);

        Map<String, String> clientResponse = buildClientResponse(hash);

        return ok(Json.toJson(clientResponse));
    }

    public static Result reroute(String hash) throws IOException {

        System.out.println(hash);

        Map<String, String> clientResponse = buildClientResponse(hash);
        
        //http://stackoverflow.com/questions/10962694/how-to-redirect-to-external-url-in-play-framework-2-0-java
        String redirectUrl = clientResponse.get("url");
        System.out.println(redirectUrl);
        return redirect(redirectUrl);

    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result shorten() throws IOException, NoSuchAlgorithmException {

        JsonNode json = request().body().asJson();
        String url = json.findPath("url").textValue().trim();
        System.out.println(url);

        if (!url.matches("^(http|https)://.*$")) {
            Map<String, String> clientResponse = new LinkedHashMap<String, String>();
            clientResponse.put("url", url);
            clientResponse.put("error", "The provided URL: '" + url + "' does not contain a valid protocol; valid protocols are: http. https");

            return ok(Json.toJson(clientResponse));
        }

        // build md5 hash for the url
        String hash = buildHashForUrl(url);

        Map<String, String> clientResponse = buildClientResponse(hash);

        if (Boolean.valueOf(clientResponse.get("success"))) {

        } else {

            System.out.println("no existing value found for hash: " + hash);
            System.out.println("attempting to index for hash: " + hash);

            indexHashForUrl(url, hash);

            clientResponse.put("created", "true");

        }

        System.out.println(clientResponse.toString());
        return ok(Json.toJson(clientResponse));

    }

    private static String buildHashForUrl(String url) throws NoSuchAlgorithmException {

        String s = url;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(s.getBytes(), 0, s.length());
        String hash = new BigInteger(1, md.digest()).toString(16);
        System.out.println("MD5: " + hash);

        return hash;
    }

    public static JestClient getJestClient() {

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .defaultCredentials("elastic", "changeme")
                .multiThreaded(true)
                .build());
        JestClient client = factory.getObject();

        return client;

    }

    public static Map<String, String> lookupUrlForHash(String hash) throws IOException {

        JestClient client = getJestClient();

        Map<String, String> lookupResult = new LinkedHashMap<String,String>();

        //ElasticSearch GET call via Jest Library
        Get get = new Get.Builder("shorty", hash).type("url").build();
        JestResult result = client.execute(get);
        JsonObject jsonResponse = result.getJsonObject();

        if (jsonResponse.has("found")) {

            Boolean resultBoolean = jsonResponse.get("found").getAsBoolean();

            if (resultBoolean) {

                System.out.println("existing value found for hash: " + hash);
                System.out.println(jsonResponse.toString());

                if (jsonResponse.has("_source")) {

                    JsonObject sourceJson = jsonResponse.get("_source").getAsJsonObject();
                    String url = sourceJson.get("url").getAsString();

                    lookupResult.put("url", url);
                    lookupResult.put("success", "true");

                    System.out.println("returning url to the user: " + url);  //+ url.toString());

                }

            } else {

                String url = "no existing match found for hash: " + hash;
                System.out.println("url");
                lookupResult.put("url", url);
                lookupResult.put("success", "false");

            }
        }

        return lookupResult;
    }

    public static void indexHashForUrl(String url, String hash) throws IOException {

        JestClient client = getJestClient();

        Map<String, String> elasticsearchPayload = new LinkedHashMap<String, String>();
        elasticsearchPayload.put("url", url);
        elasticsearchPayload.put("hash", hash);

        Index index = new Index.Builder(elasticsearchPayload)
                .index("shorty")
                .type("url")
                .id(hash)
                .setParameter(Parameters.REFRESH, true)
                .build();

        System.out.println(elasticsearchPayload);

        //TODO: add error handling for elasticsearch
        //TODO: add proper return value
        client.execute(index);

        return;


    }

    private static Map<String, String> buildClientResponse(String hash) throws IOException {

        Map<String, String> lookupResult = lookupUrlForHash(hash);
        Map<String, String> clientResponse = new LinkedHashMap<String,String>();

        if (Boolean.valueOf(lookupResult.get("success"))) {
            clientResponse.put("hash", hash);
            clientResponse.put("url", lookupResult.get("url"));
            clientResponse.put("found", "true");
        } else {
            clientResponse.put("hash", hash);
            clientResponse.put("url", lookupResult.get("url"));
            clientResponse.put("found", "false");
        }

        return clientResponse;
    }

}
