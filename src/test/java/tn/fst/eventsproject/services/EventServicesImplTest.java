package tn.fst.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.fst.eventsproject.entities.Event;
import tn.fst.eventsproject.entities.Participant;
import tn.fst.eventsproject.repositories.EventRepository;
import tn.fst.eventsproject.repositories.LogisticsRepository;
import tn.fst.eventsproject.repositories.ParticipantRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    EventRepository eventRepository;

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    LogisticsRepository logisticsRepository;

    @InjectMocks
    EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
    }

    @Test
    void addParticipant_delegatesToRepository() {
        Participant p = new Participant();
        p.setNom("Alice");
        when(participantRepository.save(any(Participant.class))).thenAnswer(i -> {
            Participant arg = i.getArgument(0);
            arg.setIdPart(42);
            return arg;
        });

        Participant saved = eventServices.addParticipant(p);
        assertEquals(42, saved.getIdPart());
        assertEquals("Alice", saved.getNom());
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void addAffectEvenParticipant_withId_addsEventAndSaves() {
        Participant existing = new Participant();
        existing.setIdPart(7);
        when(participantRepository.findById(7)).thenReturn(Optional.of(existing));

        Event event = new Event();
        event.setDescription("E1");

        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event saved = eventServices.addAffectEvenParticipant(event,7);

        // ensure repository save was called and returned the given event
        assertEquals("E1", saved.getDescription());
        verify(eventRepository).save(any(Event.class));
    }

}
