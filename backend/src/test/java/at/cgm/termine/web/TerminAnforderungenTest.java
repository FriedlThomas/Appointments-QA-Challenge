package at.cgm.termine.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TerminAnforderungenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void buchtEinenFreienSlotGenauEinmal() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));

        buchen(slotId, "Erste Buchung", "Kontrolle").andExpect(status().isCreated());
        fehler(buchen(slotId, "Zweite Buchung", "Kontrolle").andReturn(), 409);
    }

    @Test
    void laesstAuchBeiGleichzeitigenBuchungenNurEineBuchungZu() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> erste = executor.submit(() -> {
                start.await();
                return buchen(slotId, "Erste parallele Buchung", "Kontrolle")
                        .andReturn().getResponse().getStatus();
            });
            Future<Integer> zweite = executor.submit(() -> {
                start.await();
                return buchen(slotId, "Zweite parallele Buchung", "Kontrolle")
                        .andReturn().getResponse().getStatus();
            });

            start.countDown();
            assertThat(List.of(erste.get(), zweite.get()))
                    .containsExactlyInAnyOrder(201, 409);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void lehntEinenSlotInDerVergangenheitMit400Ab() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().minusMinutes(10));

        fehler(buchen(slotId, "Testpatient", "Kontrolle").andReturn(), 400);
    }

    @Test
    void unbekannteSlotIdErgibt404() throws Exception {
        fehler(buchen("unbekannter-slot", "Testpatient", "Kontrolle").andReturn(), 404);
    }

    @Test
    void unbekannteTerminIdErgibt404() throws Exception {
        MvcResult result = mockMvc.perform(delete("/api/appointments/unbekannter-termin"))
                .andReturn();

        fehler(result, 404);
    }

    @Test
    void fehlerantwortenHabenAusschliesslichEinNichtleeresFehlerfeld() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));

        MvcResult badRequest = buchen(slotId, "", "Kontrolle").andReturn();
        MvcResult notFound = mockMvc.perform(delete("/api/appointments/unbekannter-termin"))
                .andReturn();
        buchen(slotId, "Erste Buchung", "Kontrolle").andExpect(status().isCreated());
        MvcResult conflict = buchen(slotId, "Zweite Buchung", "Kontrolle").andReturn();

        pruefeFehlerformat(badRequest);
        pruefeFehlerformat(notFound);
        pruefeFehlerformat(conflict);
    }

    @Test
    void storniertTerminUndGibtDenSlotZurNeuenBuchungFrei() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));
        String terminId = terminId(buchen(slotId, "Testpatient", "Kontrolle")
                .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(delete("/api/appointments/{id}", terminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STORNIERT"));
        buchen(slotId, "Neue Buchung", "Kontrolle").andExpect(status().isCreated());
    }

    @Test
    void bereitsStorniertenTerminKannManNichtErneutStornieren() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));
        String terminId = terminId(buchen(slotId, "Testpatient", "Kontrolle")
                .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(delete("/api/appointments/{id}", terminId))
                .andExpect(status().isOk());
        fehler(mockMvc.perform(delete("/api/appointments/{id}", terminId)).andReturn(), 409);
    }

    @Test
    void storniertTerminWenigerAls24StundenVorBeginnNicht() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusHours(23).plusMinutes(50));
        String terminId = terminId(buchen(slotId, "Testpatient", "Kontrolle")
                .andExpect(status().isCreated()).andReturn());

        fehler(mockMvc.perform(delete("/api/appointments/{id}", terminId)).andReturn(), 409);
    }

    @Test
    void erlaubtStornierungMehrAls24StundenVorBeginn() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusHours(24).plusMinutes(10));
        String terminId = terminId(buchen(slotId, "Testpatient", "Kontrolle")
                .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(delete("/api/appointments/{id}", terminId))
                .andExpect(status().isOk());
    }

    @Test
    void akzeptiertPatientennamenMitEinBis100Zeichen() throws Exception {
        String slotMitEinZeichen = neuerSlot(LocalDateTime.now().plusDays(4));
        buchen(slotMitEinZeichen, "P", "Kontrolle").andExpect(status().isCreated());

        String slotMit100Zeichen = neuerSlot(LocalDateTime.now().plusDays(4));
        buchen(slotMit100Zeichen, "P".repeat(100), "Kontrolle")
                .andExpect(status().isCreated());
    }

    @Test
    void lehntLeerenOderMehrAls100ZeichenLangenPatientennamenMit400Ab() throws Exception {
        String slotFuerLeerenNamen = neuerSlot(LocalDateTime.now().plusDays(4));
        fehler(buchen(slotFuerLeerenNamen, "", "Kontrolle").andReturn(), 400);

        String slotFuerLangenNamen = neuerSlot(LocalDateTime.now().plusDays(4));
        fehler(buchen(slotFuerLangenNamen, "P".repeat(101), "Kontrolle").andReturn(), 400);
    }

    @Test
    void akzeptiertBesuchsgrundMit255Zeichen() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));

        buchen(slotId, "Testpatient", "G".repeat(255)).andExpect(status().isCreated());
    }

    @Test
    void lehntLeerenBesuchsgrundMit400Ab() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));

        fehler(buchen(slotId, "Testpatient", "").andReturn(), 400);
    }

    @Test
    void lehntBesuchsgrundMitMehrAls255ZeichenMit400Ab() throws Exception {
        String slotId = neuerSlot(LocalDateTime.now().plusDays(4));

        fehler(buchen(slotId, "Testpatient", "G".repeat(256)).andReturn(), 400);
    }

    private String neuerSlot(LocalDateTime beginn) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "arzt", "Dr. Testlauf",
                "beginn", beginn));

        MvcResult result = mockMvc.perform(post("/api/slots")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    private org.springframework.test.web.servlet.ResultActions buchen(
            String slotId, String patientenname, String grund) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "slotId", slotId,
                "patientenname", patientenname,
                "grund", grund));

        return mockMvc.perform(post("/api/appointments")
                .contentType("application/json")
                .content(body));
    }

    private String terminId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    private void fehler(MvcResult result, int erwarteterStatus) throws Exception {
        assertThat(result.getResponse().getStatus()).isEqualTo(erwarteterStatus);
        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.hasNonNull("fehler")).isTrue();
        assertThat(body.get("fehler").isTextual()).isTrue();
    }

    private void pruefeFehlerformat(MvcResult result) throws Exception {
        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.isObject()).isTrue();
        assertThat(body.size()).isEqualTo(1);
        assertThat(body.hasNonNull("fehler")).isTrue();
        assertThat(body.get("fehler").isTextual()).isTrue();
        assertThat(body.get("fehler").asText()).isNotBlank();
    }
}
