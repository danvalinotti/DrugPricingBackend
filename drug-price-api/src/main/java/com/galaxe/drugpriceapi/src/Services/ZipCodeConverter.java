package com.galaxe.drugpriceapi.src.Services;

import com.google.gson.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Objects;

public class ZipCodeConverter {
    private static Gson gson = new Gson();
    private static String api_url = "http://maps.googleapis.com/maps/api/geocode/json?";

    public static ArrayList<String> getCoords(String zipCode) {
        try {
            String url = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&q=" + zipCode;
            WebClient webClient = WebClient.create(url);

            // Make GET request to API for coords
            Mono<String> s = webClient
                    .get()
                    .retrieve().bodyToMono(String.class);
            String block = s.block();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(Objects.requireNonNull(block));
            ArrayList<String> coords = new ArrayList<>();

            // Confirm valid API response
            if (jsonElement.isJsonObject()) {
                // Parse through JSON response for coords
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonArray records = jsonObject.get("records").getAsJsonArray();
                JsonObject record = records.get(0).getAsJsonObject();
                JsonObject fields = record.get("fields").getAsJsonObject();
                String longitude = fields.get("longitude").getAsString();
                String latitude = fields.get("latitude").getAsString();
                coords.add(latitude);
                coords.add(longitude);

                return coords;
            } else {
                return coords;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
