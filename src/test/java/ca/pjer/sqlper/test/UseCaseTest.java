package ca.pjer.sqlper.test;

import ca.pjer.sqlper.ConnectionProvider;
import ca.pjer.sqlper.Mapper;
import ca.pjer.sqlper.Sqlper;
import ca.pjer.sqlper.SqlperManager;
import ca.pjer.sqlper.support.DataSourceConnectionProvider;
import ca.pjer.sqlper.support.DefaultSqlperManager;
import ca.pjer.sqlper.support.mapper.ConverterMapper;
import org.hsqldb.jdbc.JDBCDataSource;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

public class UseCaseTest {

    public static class Account {
        private Integer id;
        private String name;
        private String email;
        private String password;
        private DateTime created;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public DateTime getCreated() {
            return created;
        }

        public void setCreated(DateTime created) {
            this.created = created;
        }
    }

    private JDBCDataSource dataSource;

    @Before
    public void init() throws Exception {
        dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:.");
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE FUNCTION BCRYPT_GEN_SALT(INTEGER) RETURNS VARCHAR(256) " +
                        "LANGUAGE JAVA DETERMINISTIC NO SQL " +
                        "EXTERNAL NAME 'CLASSPATH:org.mindrot.jbcrypt.BCrypt.gensalt'");
                statement.execute("CREATE FUNCTION BCRYPT_HASH(VARCHAR(256),VARCHAR(256)) RETURNS VARCHAR(256) " +
                        "LANGUAGE JAVA DETERMINISTIC NO SQL " +
                        "EXTERNAL NAME 'CLASSPATH:org.mindrot.jbcrypt.BCrypt.hashpw'");
                statement.execute("CREATE FUNCTION BCRYPT_CHECK(VARCHAR(256),VARCHAR(256)) RETURNS BOOLEAN " +
                        "LANGUAGE JAVA DETERMINISTIC NO SQL " +
                        "EXTERNAL NAME 'CLASSPATH:org.mindrot.jbcrypt.BCrypt.checkpw'");
            }
        }
        new DateTime(System.currentTimeMillis());
    }

    @After
    public void after() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SHUTDOWN");
            }
        }
    }

    @Test
    public void simpleCase() throws Exception {

        // Get the DataSource from somewhere.
        // Here, this.dataSource is an in memory HSQLDB DataSource (jdbc:hsqldb:mem:.)
        DataSource dataSource = this.dataSource;

        // A ConnectionProvider that provide Connection to Sqlper from the specified DataSource.
        // Sqlper does not need (nor want) to know where you fetch your connection.
        // So be free to implement yourself an evil ThreadLocalConnectionProvider as you wish :)
        ConnectionProvider connectionProvider = new DataSourceConnectionProvider(dataSource);

        // Create an SqlperManager instance using the connectionProvider above, everything else is configured by defaults.
        // This object is thread safe and can be use as a singleton in our application.
        // Since Sqlper does not generate SQL nor use esoteric JDBC feature,
        // we never need to specify a dialect or even tel Sqlper anything about our specific RDBMS.
        // We, programmers, are responsible for writing SQL statements (and using data types) that are compatible with our RDBMS.
        SqlperManager sqlperManager = new DefaultSqlperManager(connectionProvider);

        // In real life though, we rarely use things as is.
        // Indeed our Account POJO has a 'org.joda.time.DateTime created' property,
        // the org.joda.time.DateTime class is not understood by JDBC,
        // so we register a custom Mapper to use this class instead of the crude java.sql.Timestamp class.
        // This process should be relatively easy (ConverterMapper is null safe by default)!
        Mapper<DateTime> dateTimeMapper = new ConverterMapper<DateTime, Timestamp>(Timestamp.class) {

            @Override
            protected Timestamp convert(Class<DateTime> type, DateTime object) throws Exception {
                return new Timestamp(object.getMillis());
            }

            @Override
            protected DateTime convert(Timestamp object) throws Exception {
                return new DateTime(object);
            }
        };
        sqlperManager.getMappingFactory().getMapperRegistry().register(DateTime.class, dateTimeMapper);

        // Open an Sqlper "session". It can be seen as (and in fact holds) a java.sql.Connection.
        // The try-with-resources here ensure the Sqlper (and its Connection) is closed at the end.
        try (Sqlper sqlper = sqlperManager.open()) {


            // We can always drop to the underlying Connection if needed.
            sqlper.getConnection().setAutoCommit(true);


            // A simple SQL update to create our ACCOUNT table
            sqlper.update("CREATE TABLE ACCOUNT (" +
                    "ID INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 42) PRIMARY KEY, " +
                    "NAME VARCHAR(256), " +
                    "EMAIL VARCHAR(128), " +
                    "PASSWORD VARCHAR(128), " +
                    "CREATED TIMESTAMP WITH TIME ZONE)");


            // Instantiate a new Account, and set its email and password properties.
            Account account = new Account();
            account.setEmail("arthur@acme.com");
            account.setPassword("myPassword");

            // Execute an INSERT statement where the named parameters will be mapped from our Account instance,
            // also fetch the generated ID and PASSWORD and map them back to the same Account instance.
            // Note that, excepted for the named parameters, the rest of the statement is pure SQL,
            // so we can mix in any SQL function we want (ex: here BCRYPT_* is used to hash the password)
            int updateCount = sqlper.update("INSERT INTO ACCOUNT (EMAIL, PASSWORD, CREATED) " +
                    "VALUES (:email, BCRYPT_HASH(:PASSWORD, BCRYPT_GEN_SALT(8)), NOW())", account, "ID", "PASSWORD");

            assertEquals("One row was created", 1, updateCount);
            assertNotNull("Id was generated and fetched back", account.getId());
            assertNotEquals("Password was BCRYPTed and fetched back", "myPassword", account.getPassword());
            assertNull("Created is still null, we did not fetch it back", account.getCreated());


            // Refresh our Account instance from a SELECT.
            // The named parameters will be mapped from our Account instance.
            // Then all the columns from the unique row will be mapped back to the same Account instance.
            sqlper.queryOne("SELECT * FROM ACCOUNT WHERE ID = :id", account);

            assertNotNull("Created is now set, we refreshed ", account.getCreated());


            // Let our user change the name of his account
            account.setName("Arthur Philip Dent");

            // Execute an UPDATE statement where (only) the named parameters will be mapped from our Account instance.
            updateCount = sqlper.update("UPDATE ACCOUNT SET NAME = :name WHERE ID = :id", account);

            assertEquals("One row updated", 1, updateCount);


            // Get the COUNT(*) of ACCOUNT
            Long count = sqlper.queryOne("SELECT COUNT(*) FROM ACCOUNT", Long.class);

            assertEquals("A count of one since we inserted only one", Long.valueOf(1), count);


            // Create a map that contains hypothetical login form values.
            Map<String, Object> loginForm = new HashMap<>();
            loginForm.put("email", "ARTHUR@acme.com");
            loginForm.put("password", "myPassword");

            // Execute a SELECT statement where the parameters will be mapped from the above Map.
            // The method queryOne is used to return only the first row from the ResultSet.
            // Then the projection's single column will be mapped to an Integer.
            Integer accountId = sqlper.queryOne("SELECT ID FROM ACCOUNT WHERE UPPER(EMAIL) = UPPER(:email) " +
                    "AND BCRYPT_CHECK(:password, PASSWORD)", loginForm, Integer.class);

            assertEquals("The selected accountId should match the one created earlier", account.getId(), accountId);


            // Fetch all accounts where rows will be each mapped to a new Account instance,
            // accumulated and returned into a List
            List<Account> accounts = sqlper.query("SELECT * FROM ACCOUNT", Account.class);

            assertEquals("One item in the list since we inserted only one", 1, accounts.size());

            // Fetch a complicated report into a List of Map.
            List<Map> report = sqlper.query("SELECT TRUNC(CREATED, 'DD') AS creationDate, COUNT(*) AS createdCount " +
                            "FROM ACCOUNT WHERE CREATED >= :created " +
                            "GROUP BY creationDate ORDER BY creationDate",
                    Collections.singletonMap("created", DateTime.now().minusMonths(1).withTimeAtStartOfDay()),
                    Map.class);

            assertEquals("Only one line in the report", 1, report.size());
            assertThat("First line creation date is a Timestamp", report.get(0).get("creationDate"), instanceOf(Timestamp.class));
            assertEquals("First line created count is one", Long.valueOf(1), report.get(0).get("createdCount"));
        }
    }
}
