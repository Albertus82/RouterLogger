package it.albertus.router.server.json;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.dto.StatusDto;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.server.annotation.Path;
import it.albertus.router.util.Payload;

@Path("/json/status")
public class StatusJsonHandler extends BaseJsonHandler {

	public StatusJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new StatusDto(engine.getCurrentStatus()).toJson());
		addRefreshHeader(exchange);
		sendResponse(exchange, payload);
	}

}
