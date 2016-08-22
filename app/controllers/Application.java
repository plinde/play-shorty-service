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
    public static Result shorten() throws IOException, NoSuchAlgorithmException {

        JsonNode json = request().body().asJson();

        String url = json.findPath("url").textValue();

        System.out.println(url);

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .defaultCredentials("elastic", "changeme")
                .multiThreaded(true)
                .build());
        JestClient client = factory.getObject();


        Map<String, String> source = new LinkedHashMap<String, String>();
        source.put("url", url);
        System.out.println(source.toString());


        // build md5 hash for the url
        String s = url;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(s.getBytes(), 0, s.length());
        String hash = new BigInteger(1, md.digest()).toString(16);
        System.out.println("MD5: " + hash);

        source.put("hash", hash);


        //for shorten action
        // first do a query to find if we already have a match

        Get get = new Get.Builder("shorty", hash).type("url").build();
        JestResult result = client.execute(get);
        JsonObject jsonResponse = result.getJsonObject();

        System.out.println(jsonResponse.toString());

        Map<String, String> clientResponse = new LinkedHashMap<String,String>();
        clientResponse.put("url", url);
        clientResponse.put("hash", hash);

        if (jsonResponse.has("found")) {

            //if we do, then naively return the existing short URL

            Boolean resultBoolean = jsonResponse.get("found").getAsBoolean();

            if (resultBoolean) {

                System.out.println("existing value found for hash: " + hash);

                clientResponse.put("created", "false");


            } else {

                //if we don't, then return the generated short URL and persist it
                //we can use the md5hash as the _id in elasticsearch to make this more efficient?

                System.out.println("no existing value found for hash: " + hash);
                System.out.println("attempting to index for hash: " + hash);


                Index index = new Index.Builder(source)
                        .index("shorty")
                        .type("url")
                        .id(hash)
                        .setParameter(Parameters.REFRESH, true)
                        .build();

                System.out.println(source);

                client.execute(index);

                clientResponse.put("created", "true");

            }


        } else {

        // somethign went front, non JSON response

        }






        System.out.println(clientResponse.toString());
        return ok(Json.toJson(clientResponse));

    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Result enlongate() throws IOException {

        JsonNode json = request().body().asJson();

        String hash = json.findPath("hash").textValue();

        System.out.println(hash);

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .defaultCredentials("elastic", "changeme")
                .multiThreaded(true)
                .build());
        JestClient client = factory.getObject();


        // first do a query to find if we already have a match

        Get get = new Get.Builder("shorty", hash).type("url").build();
        JestResult result = client.execute(get);
        JsonObject jsonResponse = result.getJsonObject();

        Map<String, String> clientResponse = new LinkedHashMap<String,String>();
        clientResponse.put("hash", hash);

        if (jsonResponse.has("found")) {

            //if we do, then naively return the existing short URL

            Boolean resultBoolean = jsonResponse.get("found").getAsBoolean();

            if (resultBoolean) {

                System.out.println("existing value found for hash: " + hash);

                System.out.println(jsonResponse.toString());


                if (jsonResponse.has("_source")) {
//
//                    String url = jsonResponse.get("_source").getAsString();
//
                    JsonObject sourceJson = jsonResponse.get("_source").getAsJsonObject();
                    String url = sourceJson.get("url").getAsString();

                    clientResponse.put("url", url);
                    clientResponse.put("found", "true");

                    System.out.println("returning url to the user: " + url);  //+ url.toString());
                }

            } else {

                //if we don't, then return the generated short URL and persist it
                //we can use the md5hash as the _id in elasticsearch to make this more efficient?

                System.out.println("no value found for hash: " + hash);

                clientResponse.put("found", "false");


            }
        }





        //if we dont, then return message to the user

        System.out.println(clientResponse.toString());
        return ok(Json.toJson(clientResponse));
    }

}
