package io.github.flozano;

import java.util.concurrent.TimeUnit;

public class ServerConfiguration {

	private int minThreads = 10;
	private int maxThreads = 200;

	private int queueSize = 0;

	private String host = "0.0.0.0";

	private int port = 8089;

	private long gracefulStopTimeout = TimeUnit.SECONDS.toMillis(20);

	private String name = "testing";

	private int responseHeadersSize = 64 * 1024;

	private int requestHeadersSize = 64 * 1024;

	private String cacheControl =  "max-age=0, no-cache, no-store";

	public String getCacheControl() {
		return cacheControl;
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public int getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(int minThreads) {
		this.minThreads = minThreads;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	public long getGracefulStopTimeout() {
		return gracefulStopTimeout;
	}

	public void setGracefulStopTimeout(long gracefulStopTimeout) {
		this.gracefulStopTimeout = gracefulStopTimeout;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getResponseHeadersSize() {
		return responseHeadersSize;
	}

	public void setResponseHeadersSize(int responseHeadersSize) {
		this.responseHeadersSize = responseHeadersSize;
	}

	public int getRequestHeadersSize() {
		return requestHeadersSize;
	}

	public void setRequestHeadersSize(int requestHeadersSize) {
		this.requestHeadersSize = requestHeadersSize;
	}
}
