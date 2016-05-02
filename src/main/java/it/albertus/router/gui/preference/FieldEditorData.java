package it.albertus.router.gui.preference;

import java.util.Arrays;

public class FieldEditorData {

	// Generic
	private final Boolean emptyStringAllowed;

	// ComboFieldEditor
	private final String[][] comboEntryNamesAndValues;

	// ScaleFieldEditor
	private final Integer scaleMinimum;
	private final Integer scaleMaximum;
	private final Integer scaleIncrement;
	private final Integer scalePageIncrement;

	// StringFieldEditor
	private final Integer textLimit;
	private final Integer textWidth;
	private final Integer textHeight;
	private final Integer textValidateStrategy;

	// IntegerFieldEditor
	private final Integer integerMinValidValue;
	private final Integer integerMaxValidValue;

	// DirectoryFieldEditor
	private final String directoryDialogMessageKey;

	public String[][] getComboEntryNamesAndValues() {
		return comboEntryNamesAndValues;
	}

	public Integer getScaleMinimum() {
		return scaleMinimum;
	}

	public Integer getScaleMaximum() {
		return scaleMaximum;
	}

	public Integer getScaleIncrement() {
		return scaleIncrement;
	}

	public Integer getScalePageIncrement() {
		return scalePageIncrement;
	}

	public Integer getTextLimit() {
		return textLimit;
	}

	public Integer getTextWidth() {
		return textWidth;
	}

	public Integer getTextHeight() {
		return textHeight;
	}

	public Integer getTextValidateStrategy() {
		return textValidateStrategy;
	}

	public Integer getIntegerMinValidValue() {
		return integerMinValidValue;
	}

	public Integer getIntegerMaxValidValue() {
		return integerMaxValidValue;
	}

	public String getDirectoryDialogMessageKey() {
		return directoryDialogMessageKey;
	}

	public Boolean getEmptyStringAllowed() {
		return emptyStringAllowed;
	}

	@Override
	public String toString() {
		return "FieldEditorData [emptyStringAllowed=" + emptyStringAllowed + ", comboEntryNamesAndValues=" + Arrays.toString(comboEntryNamesAndValues) + ", scaleMinimum=" + scaleMinimum + ", scaleMaximum=" + scaleMaximum + ", scaleIncrement=" + scaleIncrement + ", scalePageIncrement=" + scalePageIncrement + ", textLimit=" + textLimit + ", textWidth=" + textWidth + ", textHeight=" + textHeight + ", textValidateStrategy=" + textValidateStrategy + ", integerMinValidValue=" + integerMinValidValue + ", integerMaxValidValue=" + integerMaxValidValue + ", directoryDialogMessageKey=" + directoryDialogMessageKey + "]";
	}

	public static class FieldEditorDataBuilder {
		private Boolean emptyStringAllowed;
		private String[][] comboEntryNamesAndValues;
		private Integer scaleMinimum;
		private Integer scaleMaximum;
		private Integer scaleIncrement;
		private Integer scalePageIncrement;
		private Integer textLimit;
		private Integer textWidth;
		private Integer textHeight;
		private Integer textValidateStrategy;
		private Integer integerMinValidValue;
		private Integer integerMaxValidValue;
		private String directoryDialogMessageKey;

		public FieldEditorDataBuilder emptyStringAllowed(final boolean emptyStringAllowed) {
			this.emptyStringAllowed = emptyStringAllowed;
			return this;
		}

		public FieldEditorDataBuilder comboEntryNamesAndValues(final String[][] entryNamesAndValues) {
			this.comboEntryNamesAndValues = entryNamesAndValues;
			return this;
		}

		public FieldEditorDataBuilder scaleMinimum(final int min) {
			this.scaleMinimum = min;
			return this;
		}

		public FieldEditorDataBuilder scaleMaximum(final int max) {
			this.scaleMaximum = max;
			return this;
		}

		public FieldEditorDataBuilder scaleIncrement(final int increment) {
			this.scaleIncrement = increment;
			return this;
		}

		public FieldEditorDataBuilder scalePageIncrement(final int pageIncrement) {
			this.scalePageIncrement = pageIncrement;
			return this;
		}

		public FieldEditorDataBuilder textLimit(final int limit) {
			this.textLimit = limit;
			return this;
		}

		public FieldEditorDataBuilder textWidth(final int width) {
			this.textWidth = width;
			return this;
		}

		public FieldEditorDataBuilder textHeight(final int height) {
			this.textHeight = height;
			return this;
		}

		public FieldEditorDataBuilder textValidateStrategy(final int validateStrategy) {
			this.textValidateStrategy = validateStrategy;
			return this;
		}

		public FieldEditorDataBuilder integerValidRange(final int min, final int max) {
			this.integerMinValidValue = min;
			this.integerMaxValidValue = max;
			return this;
		}

		public FieldEditorDataBuilder directoryDialogMessageKey(final String dialogMessageKey) {
			this.directoryDialogMessageKey = dialogMessageKey;
			return this;
		}

		public FieldEditorData build() {
			return new FieldEditorData(this);
		}
	}

	private FieldEditorData(final FieldEditorDataBuilder builder) {
		this.emptyStringAllowed = builder.emptyStringAllowed;
		this.comboEntryNamesAndValues = builder.comboEntryNamesAndValues;
		this.scaleMinimum = builder.scaleMinimum;
		this.scaleMaximum = builder.scaleMaximum;
		this.scaleIncrement = builder.scaleIncrement;
		this.scalePageIncrement = builder.scalePageIncrement;
		this.textLimit = builder.textLimit;
		this.textWidth = builder.textWidth;
		this.textHeight = builder.textHeight;
		this.textValidateStrategy = builder.textValidateStrategy;
		this.integerMinValidValue = builder.integerMinValidValue;
		this.integerMaxValidValue = builder.integerMaxValidValue;
		this.directoryDialogMessageKey = builder.directoryDialogMessageKey;
	}

}
