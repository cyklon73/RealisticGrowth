package de.cyklon.realisticgrowth.modrinth;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Updater {

	private static final String RESOURCE_ID = "realistic-growth";
	private static final String API_URL = "https://api.modrinth.com/v2/";
	private static final String RESOURCE_URL = "https://modrinth.com/plugin/realistic-growth";

	private final HttpClient client;
	private final Gson gson;

	public Updater() {
		this.client = HttpClient.newHttpClient();
		this.gson = new Gson();
	}

	private JsonElement request(String endpoint) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(API_URL + "project/" + RESOURCE_ID + endpoint))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) throw new IOException("Request was not successfully: (" + response.statusCode() + ") " + response.body());
		return gson.fromJson(response.body(), JsonElement.class);
	}

	private List<Version> getVersions() {
		JsonElement response;
		try {
            response = request("/version");
        } catch (IOException | InterruptedException e) {
            return Collections.emptyList();
        }
		List<Version> result = new LinkedList<>();
		for (JsonElement jsonElement : response.getAsJsonArray()) {
			result.add(gson.fromJson(jsonElement, Version.class));
		}
		return result;
    }

}
