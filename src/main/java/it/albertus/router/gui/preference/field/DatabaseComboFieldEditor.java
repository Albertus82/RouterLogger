package it.albertus.router.gui.preference.field;

import it.albertus.router.resources.Resources;

import java.sql.Driver;

import org.eclipse.swt.widgets.Composite;

public class DatabaseComboFieldEditor extends ValidatedComboFieldEditor {

	public DatabaseComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		setErrorMessage(Resources.get("err.preferences.combo.class.database.invalid"));
	}

	@Override
	protected boolean checkState() {
		if (getValue() != null && !getValue().isEmpty()) {
			try {
				final Class<?> driverClass = Class.forName(getValue());
				if (Driver.class.isAssignableFrom(driverClass) && !Driver.class.equals(driverClass)) {
					return true;
				}
				else {
					setErrorMessage(Resources.get("err.preferences.combo.class.database.invalid"));
					return false;
				}
			}
			catch (final Throwable throwable) {
				setErrorMessage(Resources.get("err.preferences.combo.class.database.missing"));
				return false;
			}
		}
		else {
			return true;
		}
	}

}
