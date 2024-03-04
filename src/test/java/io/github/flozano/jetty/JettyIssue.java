package io.github.flozano.jetty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.client.FutureResponseListener;
import org.eclipse.jetty.client.StringRequestContent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.flozano.JettyServer;
import io.github.flozano.MirrorServlet;
import io.github.flozano.ServerConfiguration;

public class JettyIssue {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JettyIssue.class);

	@ParameterizedTest
	@MethodSource("parameters")
	public void testJdkClient(int copyBufferSize, int outputBufferSize, int requestAndResponseSize)
			throws IOException, InterruptedException {
		LOGGER.info("Starting test with copyBufferSize={}, outputBufferSize={}, requestAndResponseSize={}",
				copyBufferSize, outputBufferSize, requestAndResponseSize);
		var config = new ServerConfiguration();
		config.setOutputBufferSize(outputBufferSize);
		try (JettyServer server = new JettyServer(config)) {
			server.configureServlet("mirror", new MirrorServlet(copyBufferSize));
			server.start();
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder() //
					.uri(server.getBaseURI()) //
					.header("content-type", "application/octet-stream") //
					.POST(HttpRequest.BodyPublishers.ofString(
							RandomStringUtils.randomAlphanumeric(requestAndResponseSize))).build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode());
		}
		LOGGER.info("Completed test with copyBufferSize={}, outputBufferSize={}, requestAndResponseSize={}",
				copyBufferSize, outputBufferSize, requestAndResponseSize);
	}

	@ParameterizedTest
	@MethodSource("parameters")
	public void testJettyClient(int copyBufferSize, int outputBufferSize, int requestAndResponseSize) throws Exception {
		LOGGER.info("Starting Jetty-client test with copyBufferSize={}, outputBufferSize={}, requestAndResponseSize={}",
				copyBufferSize, outputBufferSize, requestAndResponseSize);
		var config = new ServerConfiguration();
		config.setOutputBufferSize(outputBufferSize);
		try (JettyServer server = new JettyServer(config)) {
			server.configureServlet("mirror", new MirrorServlet(copyBufferSize));
			server.start();
			var httpClient = new org.eclipse.jetty.client.HttpClient();
			httpClient.setRequestBufferSize(15_000_000);
			httpClient.setResponseBufferSize(15_000_000);
			try {
				httpClient.start();
				var request = httpClient.newRequest(server.getBaseURI()).method("POST").body(
						new StringRequestContent("application/octet-stream",
								RandomStringUtils.randomAlphanumeric(requestAndResponseSize), StandardCharsets.UTF_8));
				var listener = new FutureResponseListener(request, 5_000_000);
				request.send(listener);
				var response = listener.get();
				assertEquals(200, response.getStatus());
			} finally {
				httpClient.stop();
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
