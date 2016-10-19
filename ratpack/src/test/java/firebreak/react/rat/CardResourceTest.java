package firebreak.react.rat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;

import static firebreak.react.rat.App.serverSpec;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CardResourceTest {

    private EmbeddedApp app;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() throws Exception {
        app = EmbeddedApp.of(serverSpec());
    }

    @Test
    public void shouldSayHello() throws Exception {
        app.test(client -> {
            String mes = client.get().getBody().getText();
            JsonNode message = mapper.readTree(mes);
            assertThat(message.get("message").asText(), is("Hello from (Dean Martin)"));
        });
    }

    @Test
    public void shouldAuthorise() throws Exception {
        ImmutableMap<String, String> cardData = ImmutableMap.of("cardHolder", "holder", "cardNumber", "number", "cvc", "123", "address", "EC2 4RT");
        app.test(client -> {
            ReceivedResponse receivedResponse = client.requestSpec(requestSpec ->
                    requestSpec.body(body -> body.text(mapper.writeValueAsString(cardData))))
                    .post(format("/authorise/%s", "7473458"));

            assertThat(receivedResponse.getStatusCode(), is(200));
            JsonNode message = mapper.readTree(receivedResponse.getBody().getText());
            assertThat(message.get("message").asText(), is("success"));
        });
    }

    @Test
    public void shouldAuthoriseObserve() throws Exception {
        ImmutableMap<String, String> cardData = ImmutableMap.of("cardHolder", "holder", "cardNumber", "number", "cvc", "123", "address", "EC2 4RT");
        app.test(client -> {
            ReceivedResponse receivedResponse = client.requestSpec(requestSpec ->
                    requestSpec.body(body -> body.text(mapper.writeValueAsString(cardData))))
                    .post(format("/authoriseObserve/%s", "7473458"));

            Thread.sleep(3000);
            assertThat(receivedResponse.getStatusCode(), is(202));
            JsonNode message = mapper.readTree(receivedResponse.getBody().getText());
            assertThat(message.get("message").asText(), is("timedout error"));
        });
    }

    @After
    public void after() throws Exception {
        app.close();
    }
}
