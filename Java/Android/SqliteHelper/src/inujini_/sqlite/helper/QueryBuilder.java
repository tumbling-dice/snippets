package inujini_.sqlite.helper;

import inujini_.sqlite.meta.ColumnProperty;
import inujini_.sqlite.meta.ISqlite;


public class QueryBuilder {

	private StringBuilder _query;

	public QueryBuilder() {
		_query = new StringBuilder();
	}

	public QueryBuilder select(ColumnProperty... column) {
		_query.append("SELECT");

		for (int i = 0, count = column.length; i < count; i++) {
			if (i == 0) {
				_query.append(" ").append(column[i].getColumnName());
			} else {
				_query.append(" ,").append(column[i].getColumnName());
			}
		}

		return this;
	}

	public UpdateQuery update(String table) {
		_query.append("UPDATE ").append(table).append(" SET");
		return new UpdateQuery(this);
	}

	public WhereQuery delete(String table) {
		_query.append("DELETE FROM ").append(table);
		return new WhereQuery(this);
	}

	public QueryBuilder insert(String table, ColumnValuePair... values) {
		_query.append("INSERT INTO ").append(table).append("(");
		StringBuilder valuesBuilder = new StringBuilder("(");

		for (ColumnValuePair columnValuePair : values) {
			ColumnProperty c = columnValuePair.getColumn();

			_query.append(c.getColumnName()).append(",");

			String value = columnValuePair.getValue();

			valuesBuilder.append(c.getType() == ISqlite.FIELD_TEXT
					? "'" + value + "'"
					: value)
					.append(",");
		}

		_query.deleteCharAt(_query.length() - 1);
		valuesBuilder.deleteCharAt(valuesBuilder.length() - 1);

		valuesBuilder.append(")");
		_query.append(") VALUES").append(valuesBuilder);

		return this;
	}

	public QueryBuilder from(String table) {
		_query.append(" FROM ").append(table);
		return this;
	}

	public QueryBuilder limit(int count) {
		_query.append(" LIMIT ").append(count);
		return this;
	}

	public QueryBuilder offset(int count) {
		_query.append(" OFFSET ").append(count);
		return this;
	}

	public QueryBuilder orderByAsc(ColumnProperty... columns) {
		_query.append("ORDER BY");
		for (ColumnProperty column : columns) {
			_query.append(column.getColumnName()).append(",");
		}

		_query.deleteCharAt(_query.length() - 1);

		return this;
	}

	public QueryBuilder orderByDesc(ColumnProperty... columns) {
		orderByAsc(columns);
		_query.append(" DESC");

		return this;
	}

	public WhereQuery where() {
		return new WhereQuery(this);
	}

	private QueryBuilder append(ColumnProperty c) {
		_query.append(c.getColumnName());
		return this;
	}

	private QueryBuilder append(String s) {
		_query.append(s);
		return this;
	}

	@Override
	public String toString() {
		return _query.append(";").toString();
	}

	public class UpdateQuery {
		private QueryBuilder _qb;
		private boolean isSet = false;

		public UpdateQuery(QueryBuilder queryBuilder) {
			_qb = queryBuilder;
		}

		public UpdateQuery set(ColumnProperty column, String value) {
			_qb.append(" ");
			if(isSet) _qb.append(",");

			_qb.append(column.getColumnName()).append(" = ")
				.append(column.getType() == ISqlite.FIELD_TEXT
						? "'" + value + "'"
						: value);

			isSet = true;
			return this;
		}

		public UpdateQuery set(ColumnValuePair... columns) {
			for (ColumnValuePair column : columns) {
				set(column.getColumn(), column.getValue());
			}
			return this;
		}

		public WhereQuery where() {
			return new WhereQuery(_qb);
		}

		@Override
		public String toString() {
			return _qb.toString();
		}
	}

	public class WhereQuery {

		private QueryBuilder _qb;

		public WhereQuery(QueryBuilder queryBuilder) {
			_qb = queryBuilder.append(" WHERE");
		}

		public WhereQuery equal(ColumnProperty column, String value) {
			_qb.append(" ")
					.append(column)
					.append(" = ")
					.append(column.getType() == ISqlite.FIELD_TEXT
							? "'" + value + "'"
							: value);
			return this;
		}

		public WhereQuery in(ColumnProperty column, String... values) {
			_qb.append(" ").append(column).append(" IN (");

			for (int i = 0, count = values.length; i < count; i++) {
				if (i == 0) {
					_qb.append(column.getType() == ISqlite.FIELD_TEXT
							? "'" + values[i] + "'"
							: values[i]);
				} else {
					_qb.append(",")
						.append(column.getType() == ISqlite.FIELD_TEXT
								? "'" + values[i] + "'"
								: values[i]);
				}
			}

			_qb.append(")");
			return this;
		}

		public WhereQuery and() {
			_qb.append(" AND");
			return this;
		}

		public WhereQuery or() {
			_qb.append(" OR");
			return this;
		}

		public QueryBuilder toQuery() {
			return _qb;
		}

		@Override
		public String toString() {
			return _qb.toString();
		}

	}
}
