package it.albertus.router;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Threshold {

	public enum Type {
		NOT_EQUAL("<>", new String[] { "ne", "!=", "<>", "^=" }),
		LESS_THAN("<", new String[] { "lt", "<" }),
		LESS_OR_EQUAL("<=", new String[] { "le", "<=" }),
		EQUAL("=", new String[] { "eq", "==", "=" }),
		GREATER_OR_EQUAL(">=", new String[] { "ge", ">=" }),
		GREATER_THAN(">", new String[] { "gt", ">" });

		private final String description;
		private final Set<String> abbreviations = new HashSet<String>();

		private Type(String description, String[] abbreviations) {
			this.description = description;
			this.abbreviations.addAll(Arrays.asList(abbreviations));
		}

		public String getDescription() {
			return description;
		}

		public Set<String> getAbbreviations() {
			return abbreviations;
		}

		@Override
		public String toString() {
			return description;
		}

		public static Type getEnum(String abbreviation) {
			if (abbreviation != null) {
				for (Type type : Type.values()) {
					if (type.name().equalsIgnoreCase(abbreviation.trim()) || type.abbreviations.contains(abbreviation.trim().toLowerCase())) {
						return type;
					}
				}
			}
			return null;
		}
	}

	private final String key;
	private final Type type;
	private final String value;

	public Threshold(String key, Type type, String value) {
		this.key = key;
		this.type = type;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean isReached(final String value) {
		if (value != null) {
			Comparable threshold;
			Comparable current;
			try { // Tento la conversione a Number...
				threshold = Double.valueOf(this.value);
				current = Double.valueOf(value);
			}
			catch (NumberFormatException nfe) {
				threshold = this.value;
				current = value;
			}
			switch (type) {
			case EQUAL:
				return current.equals(threshold);
			case GREATER_OR_EQUAL:
				return current.compareTo(threshold) >= 0;
			case GREATER_THAN:
				return current.compareTo(threshold) > 0;
			case LESS_OR_EQUAL:
				return current.compareTo(threshold) <= 0;
			case LESS_THAN:
				return current.compareTo(threshold) < 0;
			case NOT_EQUAL:
				return !current.equals(threshold);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Threshold)) {
			return false;
		}
		Threshold other = (Threshold) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		}
		else if (!key.equals(other.key)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return key + ' ' + type + ' ' + value;
	}

}