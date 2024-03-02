package io.github.flozano.jetty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import io.github.flozano.JettyServer;
import io.github.flozano.MirrorServlet;
import io.github.flozano.ServerConfiguration;

public class JettyIssue {

	@Test
	public void test() throws IOException, InterruptedException {
		try (JettyServer server = new JettyServer(new ServerConfiguration())) {
			server.configureServlet("mirror", new MirrorServlet(1024));
			server.start();

			try (HttpClient client = HttpClient.newHttpClient()) {
				HttpRequest request = HttpRequest.newBuilder().uri(server.getBaseURI()).header("content-type",
								"application/octet-stream").POST(
								HttpRequest.BodyPublishers.ofString(RandomStringUtils.randomAlphanumeric(2 * 1024 * 1024)))
						.build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				assertEquals(200, response.statusCode());
			}
		}
	}
}