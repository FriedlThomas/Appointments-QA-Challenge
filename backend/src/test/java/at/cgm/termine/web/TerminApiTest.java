package at.cgm.termine.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TerminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postSlotsLegtEinenSlotAnUndGetSlotsLiefertIhnAlsFrei() throws Exception {
        LocalDateTime beginn = LocalDateTime.now().plusDays(4);
        MvcResult angelegt = mockMvc.perform(post("/api/slots")
                        .contentType("application/json")
                        .content(json(Map.of("arzt", "Dr. API-Test", "beginn", beginn))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.arzt").value("Dr. API-Test"))
                .andExpect(jsonPath("$.gebucht").value(false))
                .andReturn();
        String slotId = feld(angelegt, "id");

        JsonNode slots = objectMapper.readTree(mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode slot = finde(slots, slotId);

        assertThat(slot.get("arzt").asText()).isEqualTo("Dr. API-Test");
        assertThat(slot.get("gebucht").asBoolean()).isFalse();
    }

    @Test
    void getSlotsLiefertDieAnzahlFreierSlotsNachSlotanlageUndBuchungKorrekt() throws Exception {
        long freieSlotsVorher = anzahlFreierSlots(getSlots());

        String freierSlot = neuerSlot();
        String zuBuchenderSlot = neuerSlot();
        mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(json(Map.of(
                                "slotId", zuBuchenderSlot,
                                "patientenname", "API-Testpatient",
                                "grund", "Kontrolle"))))
                .andExpect(status().isCreated());

        JsonNode slotsDanach = getSlots();
        assertThat(anzahlFreierSlots(slotsDanach)).isEqualTo(freieSlotsVorher + 1);
        assertThat(finde(slotsDanach, freierSlot).get("gebucht").asBoolean()).isFalse();
        assertThat(finde(slotsDanach, zuBuchenderSlot).get("gebucht").asBoolean()).isTrue();
    }

    @Test
    void postAppointmentsBuchtDenSlotUndGetAppointmentsLiefertBuchungMitStatus() throws Exception {
        String slotId = neuerSlot();

        MvcResult gebucht = mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(json(Map.of(
                                "slotId", slotId,
                                "patientenname", "API-Testpatient",
                                "grund", "Kontrolle"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.slotId").value(slotId))
                .andExpect(jsonPath("$.patientenname").value("API-Testpatient"))
                .andExpect(jsonPath("$.grund").value("Kontrolle"))
                .andExpect(jsonPath("$.status").value("GEBUCHT"))
                .andReturn();
        String terminId = feld(gebucht, "id");

        JsonNode termine = objectMapper.readTree(mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode termin = finde(termine, terminId);
        assertThat(termin.get("slotId").asText()).isEqualTo(slotId);
        assertThat(termin.get("status").asText()).isEqualTo("GEBUCHT");

        JsonNode slots = objectMapper.readTree(mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(finde(slots, slotId).get("gebucht").asBoolean()).isTrue();
    }

    @Test
    void getAppointmentsLiefertNachEinerBuchungGenauEinenZusaetzlichenTermin() throws Exception {
        long termineVorher = getAppointments().size();
        String slotId = neuerSlot();

        MvcResult gebucht = mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(json(Map.of(
                                "slotId", slotId,
                                "patientenname", "API-Zaehler",
                                "grund", "Kontrolle"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode termineDanach = getAppointments();
        assertThat(termineDanach.size()).isEqualTo(termineVorher + 1);
        JsonNode termin = finde(termineDanach, feld(gebucht, "id"));
        assertThat(termin.get("slotId").asText()).isEqualTo(slotId);
        assertThat(termin.get("status").asText()).isEqualTo("GEBUCHT");
    }

    @Test
    void deleteAppointmentStorniertTerminUndGibtSlotWiederFrei() throws Exception {
        String slotId = neuerSlot();
        String terminId = feld(mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(json(Map.of(
                                "slotId", slotId,
                                "patientenname", "API-Testpatient",
                                "grund", "Kontrolle"))))
                .andExpect(status().isCreated())
                .andReturn(), "id");

        mockMvc.perform(delete("/api/appointments/{id}", terminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(terminId))
                .andExpect(jsonPath("$.status").value("STORNIERT"));

        JsonNode termine = objectMapper.readTree(mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(finde(termine, terminId).get("status").asText()).isEqualTo("STORNIERT");

        JsonNode slots = objectMapper.readTree(mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(finde(slots, slotId).get("gebucht").asBoolean()).isFalse();
    }

    private String neuerSlot() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/slots")
                        .contentType("application/json")
                        .content(json(Map.of(
                                "arzt", "Dr. API-Test",
                                "beginn", LocalDateTime.now().plusDays(4)))))
                .andExpect(status().isCreated())
                .andReturn();
        return feld(result, "id");
    }

    private JsonNode getSlots() throws Exception {
        return objectMapper.readTree(mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode getAppointments() throws Exception {
        return objectMapper.readTree(mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private long anzahlFreierSlots(JsonNode slots) {
        long freieSlots = 0;
        for (JsonNode slot : slots) {
            if (!slot.get("gebucht").asBoolean()) {
                freieSlots++;
            }
        }
        return freieSlots;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String feld(MvcResult result, String name) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get(name).asText();
    }

    private JsonNode finde(JsonNode array, String id) {
        for (JsonNode element : array) {
            if (id.equals(element.path("id").asText())) {
                return element;
            }
        }
        throw new AssertionError("Kein API-Element mit ID " + id + " gefunden.");
    }
}
