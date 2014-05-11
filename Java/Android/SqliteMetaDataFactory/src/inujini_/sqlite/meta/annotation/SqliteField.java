package inujini_.sqlite.meta.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqliteField {
	int type();
	String name();
	boolean notNull() default false;
	boolean primary() default false;
	boolean autoincrement() default false;
	boolean unique() default false;
	String indexName() default "";
	String defaultValue() default "";
}
