package it.albertus.router.email;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class ThresholdEmailItem implements Serializable {

	private static final long serialVersionUID = -8330502294900146719L;

	protected static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	private final Date date;
	private final Map<String, String> thresholds = new TreeMap<String, String>();
	private final RouterData routerData;

	public ThresholdEmailItem(final Map<Threshold, String> thresholdsReached, final RouterData routerData) {
		this.routerData = routerData;
		this.date = routerData.getTimestamp();
		for (final Threshold threshold : thresholdsReached.keySet()) {
			thresholds.put(threshold.getKey(), thresholdsReached.get(threshold));
		}
	}

	public Date getDate() {
		return date;
	}

	public Map<String, String> getThresholds() {
		return thresholds;
	}

	public RouterData getRouterData() {
		return routerData;
	}

	@Override
	public String toString() {
		return new StringBuilder(dateFormat.format(date)).append(" - ").append(Messages.get("msg.thresholds.reached", thresholds)).append(NewLine.CRLF.toString()).append(NewLine.CRLF.toString()).append(routerData).toString();
	}

}
