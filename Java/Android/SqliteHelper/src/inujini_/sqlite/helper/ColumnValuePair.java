package inujini_.sqlite.helper;

import inujini_.sqlite.meta.ColumnProperty;

public class ColumnValuePair {

	private ColumnProperty column;
	private String value;

	public ColumnValuePair(ColumnProperty column, String value) {
		this.column = column;
		this.value = value;
	}

	public ColumnProperty getColumn() {
		return column;
	}

	public String getValue() {
		return value;
	}

}
