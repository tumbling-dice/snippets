package inujini_.sqlite.meta.factory;



import java.util.List;

class MetaTable {
	private String tableName;
	private boolean hasPrimaryId;
	private List<MetaField> fields;
	private String packageName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean hasPrimaryId() {
		return hasPrimaryId;
	}

	public void setHasPrimaryId(boolean hasPrimaryId) {
		this.hasPrimaryId = hasPrimaryId;
	}

	public List<MetaField> getFields() {
		return fields;
	}

	public void setFields(List<MetaField> fields) {
		this.fields = fields;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
