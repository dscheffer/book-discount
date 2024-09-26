package com.dscheffer.bookdiscount;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {BookDiscountApplication.class})
@AutoConfigureMockMvc
public class PriceCalculatorControllerIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driverClassName", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name="{0}")
    @MethodSource("provideCalculatePriceData")
    void testCalculateShoppingCardPrice(
            String displayName,
            String requestBody,
            String expectedResult
    ) throws Exception {
        mockMvc.perform(post("/api/price/calculate").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                ).andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    private static Stream<Arguments> provideCalculatePriceData() {
        return Stream.of(
                Arguments.of(
                        "Ein einzelnes Buch ohne Rabatt",
                        """
                        [
                          {
                            "bookId": 1,
                            "quantity": 1
                          }
                        ]
                        """,
                        "8.00"
                ),
                Arguments.of(
                        "Ein einzelnes Buch mit Menge 0",
                        """
                        [
                          {
                            "bookId": 1,
                            "quantity": 0
                          }
                        ]
                        """,
                        "0.00"
                ),
                Arguments.of(
                        "Buch 1 mal 2; Buch 2 mal 2; Buch 3 mal 2; Buch 4 mal 1; Buch 5 mal 1",
                        """
                        [
                          {
                            "bookId": 1,
                            "quantity": 2
                          },
                          {
                            "bookId": 2,
                            "quantity": 2
                          },
                          {
                            "bookId": 3,
                            "quantity": 2
                          },
                          {
                            "bookId": 4,
                            "quantity": 1
                          },
                          {
                            "bookId": 5,
                            "quantity": 1
                          }
                        ]
                        """,
                        "51.20"
                )
        );
    }
}
