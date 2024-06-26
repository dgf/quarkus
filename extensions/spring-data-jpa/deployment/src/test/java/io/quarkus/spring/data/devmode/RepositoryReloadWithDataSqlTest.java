package io.quarkus.spring.data.devmode;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;

public class RepositoryReloadWithDataSqlTest {

    @RegisterExtension
    static QuarkusDevModeTest TEST = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("application.properties")
                    .addAsResource("import_books.sql", "data.sql")
                    .addClasses(Book.class, BookRepository.class, BookResource.class));

    @Test
    public void testRepositoryIsReloaded() {
        RestAssured.get("/book").then()
                .statusCode(200)
                .body(containsString("Strangers"), containsString("Ascent"), containsString("Everything"));

        TEST.modifySourceFile("BookRepository.java", s -> s.replace("// <placeholder>",
                "java.util.Optional<Book> findById(Integer id);"));

        TEST.modifySourceFile("BookResource.java", s -> s.replace("// <placeholder>",
                "@GET @Path(\"/{id}\") @Produces(MediaType.APPLICATION_JSON)\n" +
                        "    public java.util.Optional<Book> findById(@jakarta.ws.rs.PathParam(\"id\") Integer id) {\n" +
                        "        return bookRepository.findById(id);\n" +
                        "    }"));

        RestAssured.get("/book/1").then()
                .statusCode(200)
                .body(containsString("Strangers"));
    }
}
