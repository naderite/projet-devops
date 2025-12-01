package tn.fst.eventsproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.fst.eventsproject.entities.Event;
import tn.fst.eventsproject.entities.Logistics;
import tn.fst.eventsproject.entities.Participant;
import tn.fst.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventRestController.class)
class EventRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    IEventServices eventServices;

    @Test
    void addParticipant_returnsSavedParticipant() throws Exception {
        Participant toSave = new Participant();
        toSave.setNom("Doe");
        toSave.setPrenom("John");

        Participant saved = new Participant();
        saved.setIdPart(1);
        saved.setNom("Doe");
        saved.setPrenom("John");

        when(eventServices.addParticipant(any(Participant.class))).thenReturn(saved);

        mockMvc.perform(post("/event/addPart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toSave)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(saved)));
    }

    @Test
    void addEventWithParticipantId_returnsEvent() throws Exception {
        Event event = new Event();
        event.setDescription("Workshop");

        Event savedEvent = new Event();
        savedEvent.setIdEvent(1);
        savedEvent.setDescription("Workshop");

        when(eventServices.addAffectEvenParticipant(any(Event.class), anyInt())).thenReturn(savedEvent);

        mockMvc.perform(post("/event/addEvent/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedEvent)));
    }

    @Test
    void addEvent_returnsEvent() throws Exception {
        Event event = new Event();
        event.setDescription("Conference");

        Event savedEvent = new Event();
        savedEvent.setIdEvent(1);
        savedEvent.setDescription("Conference");

        when(eventServices.addAffectEvenParticipant(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/event/addEvent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedEvent)));
    }

    @Test
    void addAffectLog_returnsLogistics() throws Exception {
        Logistics logistics = new Logistics();
        logistics.setDescription("Projector");
        logistics.setReserve(true);
        logistics.setPrixUnit(100.0f);
        logistics.setQuantite(2);

        Logistics savedLogistics = new Logistics();
        savedLogistics.setIdLog(1);
        savedLogistics.setDescription("Projector");
        savedLogistics.setReserve(true);
        savedLogistics.setPrixUnit(100.0f);
        savedLogistics.setQuantite(2);

        when(eventServices.addAffectLog(any(Logistics.class), anyString())).thenReturn(savedLogistics);

        mockMvc.perform(put("/event/addAffectLog/TestEvent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logistics)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedLogistics)));
    }

    @Test
    void getLogistiquesDates_returnsLogisticsList() throws Exception {
        Logistics log1 = new Logistics();
        log1.setIdLog(1);
        log1.setDescription("Projector");
        log1.setReserve(true);

        Logistics log2 = new Logistics();
        log2.setIdLog(2);
        log2.setDescription("Speaker");
        log2.setReserve(true);

        List<Logistics> logisticsList = Arrays.asList(log1, log2);

        when(eventServices.getLogisticsDates(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(logisticsList);

        mockMvc.perform(get("/event/getLogs/2025-01-01/2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(logisticsList)));
    }

}
