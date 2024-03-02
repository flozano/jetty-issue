package io.github.flozano;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;

public class JettyServer implements AutoCloseable {
	private static final AtomicInteger COUNTER = new AtomicInteger();

	int port = 8080;
	String ip = "127.0.0.1";

	private final Server server;
	private final QueuedThreadPool threadPool;
	private final ServletContextHandler contextHandler;

	private ServletHolder servletHolder = null;

	public JettyServer(ServerConfiguration config) {
		this.ip = config.getHost();
		this.port = config.getPort();
		threadPool = buildQueuedThreadPool(config);
		server = buildServer(threadPool);

		HttpConnectionFactory httpFactory = buildHttpConnectionFactory(config);
		ServerConnector connector = new ServerConnector(server, httpFactory);
		connector.setPort(port);
		connector.setHost(ip);
		server.addConnector(connector);

		contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
		contextHandler.getServletHandler().setStartWithUnavailable(false);
		server.setHandler(contextHandler);
		server.setStopAtShutdown(true);
		server.setStopTimeout(config.getGracefulStopTimeout());
	}

	private static Server buildServer(QueuedThreadPool threadPool) {
		return new Server(threadPool);
	}

	private static QueuedThreadPool buildQueuedThreadPool(ServerConfiguration config) {
		final QueuedThreadPool tp = new QueuedThreadPool(config.getMaxThreads(), config.getMinThreads());
		tp.setName(config.getName() + "-jetty-" + COUNTER.incrementAndGet());
		return tp;
	}

	private static HttpConnectionFactory buildHttpConnectionFactory(ServerConfiguration config) {
		return new HttpConnectionFactory(buildHttpConfiguration(config));
	}

	private static HttpConfiguration buildHttpConfiguration(ServerConfiguration config) {
		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setResponseHeaderSize(config.getResponseHeadersSize());
		httpConfig.setRequestHeaderSize(config.getRequestHeadersSize());
		return httpConfig;
	}

	public String getBaseURL() {
		String address = ip == null || ip.equals("0.0.0.0") ? "127.0.0.1" : ip;
		return "http://" + address + ":" + port + "/";
	}

	public URI getBaseURI() {
		return URI.create(getBaseURL());
	}

	public void configureServlet(String name, Servlet servlet, boolean loadOnStartup,
			Map<String, String> servletConfiguration, String mapping, MultipartConfigElement multipartConfig) {
		servletHolder = new ServletHolder(servlet);
		servletHolder.setInitParameters(servletConfiguration);
		servletHolder.setName(name);
		servletHolder.setInitOrder(loadOnStartup ? 1 : -1);
		servletHolder.setAsyncSupported(true);
		if (multipartConfig != null) {
			servletHolder.getRegistration().setMultipartConfig(multipartConfig);
		}
		contextHandler.addServlet(servletHolder, mapping);
	}

	public void configureServlet(String name, Servlet servlet, Map<String, String> servletConfiguration,
			String mapping) {
		configureServlet(name, servlet, true, servletConfiguration, mapping, null);
	}

	public void configureServlet(String name, Servlet servlet) {
		configureServlet(name, servlet, Collections.emptyMap(), "/*");
	}

	public void start() {
		try {
			server.start();
			port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		} catch (Exception e) {
			if (server.isFailed()) {
				try {
					server.stop();
				} catch (Exception e1) {
					e1.printStackTrace(System.err);
				}
			}
			throw new RuntimeException(e);
		}
		if (servletHolder != null && servletHolder.getUnavailableException() != null) {
			throw new RuntimeException("Embedded jetty start failed", servletHolder.getUnavailableException());
		}
	}

	@Override
	public void close() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
