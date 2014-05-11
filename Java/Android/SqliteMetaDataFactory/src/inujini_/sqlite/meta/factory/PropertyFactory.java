package inujini_.sqlite.meta.factory;

import inujini_.sqlite.meta.ISqlite;
import inujini_.sqlite.meta.annotation.SqliteField;
import inujini_.sqlite.meta.annotation.SqliteTable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"inujini_.sqlite.meta.annotation.SqliteTable"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PropertyFactory extends AbstractProcessor {

	private Writer _writer = null;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		Messager messager = processingEnv.getMessager();

		try {
			// 処理対象となった型をすべて取得する
			Set<? extends Element> roots = roundEnv.getRootElements();
			for (Element root : roots) {
				// 処理対象がクラスかどうかを判定
				if (root.getKind() == ElementKind.CLASS) {
					TypeElement target = (TypeElement) root;
					// クラスに含まれているアノテーション（SqliteTable）を取得する
					SqliteTable tblAttr = target.getAnnotation(SqliteTable.class);
					if(tblAttr == null) continue;

					MetaTable metaTable = new MetaTable();
					metaTable.setTableName(tblAttr.name());
					metaTable.setHasPrimaryId(tblAttr.hasPrimaryId());
					metaTable.setPackageName(getPackageName(target));

					// 中のFieldの情報を取得する
					List<? extends Element> fieldElements = target.getEnclosedElements();
					List<MetaField> fields = new ArrayList<MetaField>();

					for (Element fieldElement : fieldElements) {
						// Filedでなければ無視する
						if(fieldElement.getKind() != ElementKind.FIELD) continue;

						SqliteField fieldAttr = fieldElement.getAnnotation(SqliteField.class);
						if(fieldAttr == null) continue;

						MetaField metaField = new MetaField();
						metaField.setAutoincrement(fieldAttr.autoincrement());
						metaField.setColumnName(fieldAttr.name());
						metaField.setDefaultValue(fieldAttr.defaultValue());
						metaField.setIndexName(fieldAttr.indexName());
						metaField.setNotNull(fieldAttr.notNull());
						metaField.setPrimary(fieldAttr.primary());
						metaField.setType(fieldAttr.type());
						metaField.setUnique(fieldAttr.unique());

						fields.add(metaField);
					}

					metaTable.setFields(fields);
					createMetaClass(metaTable);
				}
			}
		} catch(Exception e) {
			messager.printMessage(Kind.ERROR, e.getMessage());
		}

		return true;
	}

	private void createMetaClass(MetaTable table) throws IOException{
		Filer filer = processingEnv.getFiler();
		String className = "Meta" + table.getTableName();
		String fileName = table.getPackageName() + "." + className;

		try {
			JavaFileObject fileObject = filer.createSourceFile(fileName);
			_writer = fileObject.openWriter();

			write("package " + table.getPackageName() + ";");
			write("\n");
			write("import inujini_.sqlite.meta.ColumnProperty;");
			write("import inujini_.sqlite.meta.ISqlite;");
			write("\n");
			write("public final class " + className + " {");
			write("\n");
			write("\tpublic static final String TBL_NAME = \"" + table.getTableName() + "\";");
			for (MetaField field : table.getFields()) {
				StringBuilder property = new StringBuilder();

				String type = null;
				switch(field.getType()) {
					case ISqlite.FIELD_BLOB:
						type = "ISqlite.FIELD_BLOB";
						break;
					case ISqlite.FIELD_INTEGER:
						type = "ISqlite.FIELD_INTEGER";
						break;
					case ISqlite.FIELD_NULL:
						type = "ISqlite.FIELD_NULL";
						break;
					case ISqlite.FIELD_REAL:
						type = "ISqlite.FIELD_REAL";
						break;
					case ISqlite.FIELD_TEXT:
						type = "ISqlite.FIELD_TEXT";
						break;
					default:
						type = "ISqlite.FIELD_TEXT";
						break;
				}

				property.append("\"").append(field.getColumnName()).append("\", ")
					.append(type).append(", ")
					.append(field.isNotNull()).append(", ")
					.append(field.isPrimary()).append(", ")
					.append(field.isAutoincrement()).append(", ")
					.append(field.isUnique()).append(", ")
					.append("\"").append(field.getIndexName()).append("\", ")
					.append("\"").append(field.getDefaultValue()).append("\"");

				write("\tpublic static final ColumnProperty " + field.getColumnName()
						+ " = new ColumnProperty(");
				write("\t\t\t" + property.toString() + ");");
			}
			write("\tpublic static boolean hasPrimaryId() {");
			write("\t\treturn " + (table.hasPrimaryId() ? "true" : "false") + ";");
			write("\t}");

			write("}");

			_writer.flush();
		} finally {
			if(_writer != null) _writer.close();
			_writer = null;
		}
	}

	private void write(String s) throws IOException {
		_writer.write(s);
		_writer.write("\n");
	}

	private static String getPackageName(Element element) {
		String className = element.toString();
		int pos = className.lastIndexOf(".");
		return className.substring(0, pos) + ".meta";
	}

}
