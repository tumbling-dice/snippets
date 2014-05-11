package inujini_.sqlite.meta;

public class ColumnProperty {

	private String columnName;
	private int type;
	private boolean isNotNull;
	private boolean isPrimary;
	private boolean isAutoincrement;
	private boolean isUnique;
	private String indexName;
	private String defaultValue;

	public ColumnProperty(String columnName, int type, boolean isNotNull, boolean isPrimary
			, boolean isAutoincrement, boolean isUnique, String indexName, String defaultValue) {
		this.columnName = columnName;
		this.type = type;
		this.isNotNull = isNotNull;
		this.isPrimary = isPrimary;
		this.isAutoincrement = isAutoincrement;
		this.isUnique = isUnique;
		this.indexName = "".equals(indexName) ? null : indexName;
		this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getType() {
		return type;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public boolean isAutoincrement() {
		return isAutoincrement;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public String getIndexName() {
		return indexName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isNotNull() {
		return isNotNull;
	}

}
