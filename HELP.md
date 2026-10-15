# Getting Started

### Account registration and Google sign-in

The `/register` page creates a member account and sends an email verification code.
Gmail SMTP defaults to `smtp.gmail.com:587` with authentication and required
STARTTLS. Set `MAIL_USERNAME`, `MAIL_PASSWORD` (a Google App Password when using
Gmail), and optionally `MAIL_FROM` to send verification codes. `MAIL_HOST` and
`MAIL_PORT` can override the Gmail defaults for another SMTP provider. Connection,
read, and write timeouts default to five seconds.

On Windows, save credentials as user environment variables and restart the
terminal and backend so Spring Boot reloads them. Do not store credentials in
this file or commit them to source control.

Google sign-in uses OpenID Connect. Set `GOOGLE_CLIENT_ID` and
`GOOGLE_CLIENT_SECRET` in the backend environment; the Google button appears
when both values are present. In the Google Cloud OAuth client, add the exact
callback URL for the environment, for example
`http://localhost:8080/login/oauth2/code/google` for local development. Google
accounts with verified email addresses are created or linked to member accounts
and redirected to `/dashboard`.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.1/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
