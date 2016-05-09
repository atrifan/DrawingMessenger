package ro.atrifan.persistence.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

/**
 * Created by alexandru.trifan on 29.04.2016.
 */
public class CustomDialect extends PostgreSQL9Dialect {

    public CustomDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}
