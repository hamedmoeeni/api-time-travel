package hamedmoeeni.api_time_travel;

import hamedmoeeni.api_time_travel.adapter.rest.RouteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWebTestClient
@EnableWireMock({
        @ConfigureWireMock(name = "test-mock-server", port = 8888)
})
class ApiTimeTravelApplicationTests {

    @Value("${wiremock.server.baseUrl}")
    private String wireMockUrl;
    @Value("${att.management.uri-path}")
    private String managementUri;

    @Test
    void noRouting(@Autowired WebTestClient webTestClient) {
        stubFor(get("/ping").willReturn(ok("pong")));

        webTestClient.get().uri("/test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    void path(@Autowired WebTestClient webTestClient) {
        String randomResponse = "test response " + UUID.randomUUID();
        stubFor(get("/test").willReturn(ok(randomResponse)));

        webTestClient.post().uri(managementUri + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(
                        new RouteDto()
                                .setPriority(1)
                                .setSourcePath("/test")
                                .setDestinationHostUri(wireMockUrl)
                )))
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(randomResponse);
    }

    @Test
    void method(@Autowired WebTestClient webTestClient) {
        String randomResponse = "test response " + UUID.randomUUID();
        stubFor(patch("/test").willReturn(ok(randomResponse)));

        webTestClient.post().uri(managementUri + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(
                        new RouteDto()
                                .setPriority(1)
                                .setMethod("patch")
                                .setDestinationHostUri(wireMockUrl)
                )))
                .exchange()
                .expectStatus().isOk();

        webTestClient.patch().uri("/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(randomResponse);
    }

    @Test
    void host(@Autowired WebTestClient webTestClient) {
        String randomResponse = "test response " + UUID.randomUUID();
        stubFor(get("/test").willReturn(ok(randomResponse)));

        webTestClient.post().uri(managementUri + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(
                        new RouteDto()
                                .setPriority(1)
                                .setSourceHost("myHost")
                                .setDestinationHostUri(wireMockUrl)
                )))
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/test")
                .header("Host", "myHost")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(randomResponse);

        webTestClient.get().uri("/test")
                .exchange()
                .expectStatus().isNotFound();

        webTestClient.get().uri("/test")
                .header("Host", "anotherHost")
                .exchange()
                .expectStatus().isNotFound();
    }
}
