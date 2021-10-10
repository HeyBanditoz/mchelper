# How to configure IntelliJ Ultimate to run TestNG tests with database support

On GitLab's CI/CD, the test task will do the following:

1. Install MariaDB.
2. Create a dummy database to run tests on.
3. Run the unit tests, configured to use the dummy database.

If you want to run all tests before you push it to GitLab for testing by CI, you can do the following to mimic a
dummy/QA database. As this uses IntelliJ's database tools, you'll need its ultimate edition. You will also need to add
the data source you wish to use to test against into IntelliJ. Here's an example (Click the plus to add a new MariaDB
data source):
![Example](datasource_example.png)

## The Run Configuration

1. Create a new *Database Script* run configuration against your target data source with the following SQL script:

```sql
DROP DATABASE automation;
CREATE DATABASE automation COLLATE utf8mb4_general_ci;
```

2. Right-click the `test` package, and do `More Run/Debug -> Modify Run Configuration...`. This will create a default
   TestNG run configuration that you edit later.
3. In the `Environment Variables` field, punch in your database config for the unit tests to use. Here's an example:
   `HOST=127.0.0.1:3306;DB=qa;USER=root;PASS=password`. Replace the variables with your relevant values.
4. Under `Before Launch`, press `Run Another Configuration` and then your database script.

Final example:
![Example](run_configuration_example.png)

## Configuring with coverage

If you wish, you can also run your test configuration with coverage (the shield icon). To do this, on the Coverage tab
of the run configuration, change the runner to JaCoCo (as that's what the CI script uses.)

Here's an example:
![Example](coverage_configuration.png)