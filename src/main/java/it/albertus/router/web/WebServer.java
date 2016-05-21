package it.albertus.router.web;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

	public interface Defaults {
		int PORT = 8081;
		boolean ENABLED = false;
	}

	private static class Singleton {
		private static final WebServer instance = new WebServer();
	}

	public static WebServer getInstance() {
		return Singleton.instance;
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private final Authenticator authenticator = new WebServerAuthenticator();
	private HttpServer httpServer;
	private RouterLoggerEngine engine;
	private volatile boolean started = false;

	public void init(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	public void start() {
		if (!started && configuration.getBoolean("server.enabled", Defaults.ENABLED)) {
			final int port = configuration.getInt("server.port", Defaults.PORT);
			final InetSocketAddress address = new InetSocketAddress(port);
			try {
				httpServer = HttpServer.create(address, 0);
				createContexts();
				new HttpServerStartThread().start();
			}
			catch (final IOException ioe) {
				Logger.getInstance().log(new RuntimeException("Impossibile avviare il server HTTP. Verificare che la porta " + port + " non sia occupata.", ioe));
			}
		}
	}

	public void stop() {
		httpServer.stop(0);
		started = false;
	}

	public boolean isStarted() {
		return started;
	}

	private void createContexts() {
		final StatusHandler statusHandler = new StatusHandler(engine);
		httpServer.createContext(statusHandler.getPath(), statusHandler).setAuthenticator(authenticator);
	}

	private class HttpServerStartThread extends Thread {
		public HttpServerStartThread() {
			this.setName("httpServerStartThread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			httpServer.start();
			started = true;
		}
	}

}