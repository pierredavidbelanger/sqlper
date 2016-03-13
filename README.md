# Sqlper

A Java SQL helper library to map POJO to/from PreparedStatement/ResultSet

## Install

Snapshop repository (until I release the first stable version):

```xml
<repository>
    <id>snapshots-repo</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

Dependency:

```xml
<dependency>
    <groupId>ca.pjer</groupId>
    <artifactId>sqlper</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage

Imagine this POJO.

```java
public static class Account {
    private Integer id;
    private String name;
    private String email;
    private String password;
    private DateTime created;
    // ... get/set for all fields ...
}
```

Get the DataSource from somewhere. 
Here, `this.dataSource` is an in memory HSQLDB DataSource (`jdbc:hsqldb:mem:.`)

```java
DataSource dataSource = this.dataSource;
```

A `ConnectionProvider` that provide `Connection` to *Sqlper* from the specified `DataSource`.
*Sqlper* does not need (nor want) to know where you fetch your connection.
So be free to implement yourself an evil `ThreadLocalConnectionProvider` as you wish :)

```java
ConnectionProvider connectionProvider = new DataSourceConnectionProvider(dataSource);
```

Create an `SqlperManager` instance using the `connectionProvider` above, everything else is configured by defaults.
This object is thread safe and can be use as a singleton in our application.
Since *Sqlper* does not generate SQL nor use esoteric JDBC feature,
we never need to specify a dialect or even tel *Sqlper* anything about our specific RDBMS.
We, programmers, are responsible for writing SQL statements (and using data types) that are compatible with our RDBMS.

```java
SqlperManager sqlperManager = new DefaultSqlperManager(connectionProvider);
```

In real life though, we rarely use things as is.
Indeed our `Account` POJO has a `org.joda.time.DateTime created` property,
the `org.joda.time.DateTime` class is not understood by JDBC,
so we register a custom `Mapper` to use this class instead of the crude `java.sql.Timestamp` class.
This process should be relatively easy (`ConverterMapper` is null safe by default)!

```java
Mapper<DateTime> dateTimeMapper = new ConverterMapper<DateTime, Timestamp>(Timestamp.class) {

    @Override
    protected Timestamp convertToNative(Class<DateTime> fromType, DateTime object, Class<Timestamp> toType) throws Exception {
        return new Timestamp(object.getMillis());
    }

    @Override
    protected DateTime convertFromNative(Class<Timestamp> fromType, Timestamp object, Class<DateTime> toType) throws Exception {
        return new DateTime(object);
    }
};
sqlperManager.getMappingFactory().getMapperRegistry().register(DateTime.class, dateTimeMapper);
```

Open an *Sqlper* "session". It can be seen as (and in fact holds) a `java.sql.Connection`.
The try-with-resources here ensure the *Sqlper* (and its `Connection`) is closed at the end.

```java
try (Sqlper sqlper = sqlperManager.open()) {
    // sqlper usage below
}
```

We can always drop to the underlying `Connection` if needed.

```java
sqlper.getConnection().setAutoCommit(true);
```

A simple SQL update to create our `ACCOUNT` table.

```java
sqlper.update("CREATE TABLE ACCOUNT (" +
        "ID INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 42) PRIMARY KEY, " +
        "NAME VARCHAR(256), " +
        "EMAIL VARCHAR(128), " +
        "PASSWORD VARCHAR(128), " +
        "CREATED TIMESTAMP WITH TIME ZONE)");
```

Instantiate a new `Account`, and set its `email` and `password` properties.

```java
Account account = new Account();
account.setEmail("arthur@acme.com");
account.setPassword("myPassword");
```

Execute an `INSERT` statement where the named parameters will be mapped from our `Account` instance,
also fetch the generated `ID` and `PASSWORD` and map them back to the same `Account` instance.
Note that, excepted for the named parameters, the rest of the statement is pure SQL,
so we can mix in any SQL function we want (ex: here `BCRYPT_*` is used to hash the password)

```java
int updateCount = sqlper.update("INSERT INTO ACCOUNT (EMAIL, PASSWORD, CREATED) " +
        "VALUES (:email, BCRYPT_HASH(:PASSWORD, BCRYPT_GEN_SALT(8)), NOW())", account, "ID", "PASSWORD");

assertEquals("One row was created", 1, updateCount);
assertNotNull("Id was generated and fetched back", account.getId());
assertNotEquals("Password was BCRYPTed and fetched back", "myPassword", account.getPassword());
assertNull("Created is still null, we did not fetch it back", account.getCreated());
```

Refresh our `Account` instance from a `SELECT`.
The named parameters will be mapped from our `Account` instance.
Then all the columns from the unique row will be mapped back to the same `Account` instance.

```java
sqlper.queryOne("SELECT * FROM ACCOUNT WHERE ID = :id", account);

assertNotNull("Created is now set, we refreshed ", account.getCreated());
```

Let our user change the name of his account

```java
account.setName("Arthur Philip Dent");
```

Execute an `UPDATE` statement where (only) the named parameters will be mapped from our `Account` instance.

```java
updateCount = sqlper.update("UPDATE ACCOUNT SET NAME = :name WHERE ID = :id", account);

assertEquals("One row updated", 1, updateCount);
```

Get the `COUNT(*)` of `ACCOUNT`

```java
Long count = sqlper.queryOne("SELECT COUNT(*) FROM ACCOUNT", Long.class);

assertEquals("A count of one since we inserted only one", Long.valueOf(1), count);
```

Create a map that contains hypothetical login form values.

```java
Map<String, Object> loginForm = new HashMap<>();
loginForm.put("email", "ARTHUR@acme.com");
loginForm.put("password", "myPassword");
```

Execute a `SELECT` statement where the parameters will be mapped from the above `Map`.
The method `queryOne` is used to return only the first row from the `ResultSet`.
Then the projection's single column will be mapped to an `Integer`.

```java
Integer accountId = sqlper.queryOne("SELECT ID FROM ACCOUNT WHERE UPPER(EMAIL) = UPPER(:email) " +
        "AND BCRYPT_CHECK(:password, PASSWORD)", loginForm, Integer.class);

assertEquals("The selected accountId should match the one created earlier", account.getId(), accountId);
```

Fetch all accounts where rows will be each mapped to a new `Account` instance,
accumulated and returned into a `List`.

```java
List<Account> accounts = sqlper.query("SELECT * FROM ACCOUNT", Account.class);

assertEquals("One item in the list since we inserted only one", 1, accounts.size());
```

Fetch a complicated report into a `List` of `Map`.

```java
List<Map> report = sqlper.query("SELECT TRUNC(CREATED, 'DD') AS creationDate, COUNT(*) AS createdCount " +
                "FROM ACCOUNT WHERE CREATED >= :created " +
                "GROUP BY creationDate ORDER BY creationDate",
        Collections.singletonMap("created", DateTime.now().minusMonths(1).withTimeAtStartOfDay()),
        Map.class);

assertEquals("Only one line in the report", 1, report.size());
assertThat("First line creation date is a Timestamp", report.get(0).get("creationDate"), instanceOf(Timestamp.class));
assertEquals("First line created count is one", Long.valueOf(1), report.get(0).get("createdCount"));
```