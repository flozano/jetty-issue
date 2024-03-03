package io.github.flozano.jetty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.flozano.JettyServer;
import io.github.flozano.MirrorServlet;
import io.github.flozano.ServerConfiguration;

public class JettyIssue {

	@ParameterizedTest
	@MethodSource("parameters")
	public void test(int copyBufferSize, int outputBufferSize, int requestAndResponseSize)
			throws IOException, InterruptedException {
		var config = new ServerConfiguration();
		config.setOutputBufferSize(outputBufferSize);
		try (JettyServer server = new JettyServer(config)) {
			server.configureServlet("mirror", new MirrorServlet(copyBufferSize));
			server.start();

			try (HttpClient client = HttpClient.newHttpClient()) {
				HttpRequest request = HttpRequest.newBuilder() //
						.uri(server.getBaseURI()) //
						.header("content-type", "application/octet-stream") //
						.POST(HttpRequest.BodyPublishers.ofString(
								RandomStringUtils.randomAlphanumeric(requestAndResponseSize))).build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				assertEquals(200, response.statusCode());
			}
		}
	}

	public static Stream<Arguments> parameters() {
		List<Arguments> result = new ArrayList<>();
		for (int copyBufferSize : List.of(0, 8192, 1_000_000, 4_000_000)) {
			for (int outputBufferSize : List.of(1024, 32768, 1_000_000)) {
				for (int requestAndResponseSize : List.of(1_000_000, 2_000_000, 4_000_000)) {
					result.add(Arguments.of(copyBufferSize, outputBufferSize, requestAndResponseSize));
				}
			}
		}
		return result.stream();
	}

}
