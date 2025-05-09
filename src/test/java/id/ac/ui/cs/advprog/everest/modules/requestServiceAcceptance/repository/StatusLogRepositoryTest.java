package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.RequestStatus;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.StatusLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class StatusLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StatusLogRepository statusLogRepository;

    @Test
    public void testSaveStatusLog() {
        StatusLog statusLog = new StatusLog(1L, RequestStatus.PENDING, RequestStatus.REPORTED, 2L);

        StatusLog savedLog = statusLogRepository.save(statusLog);

        assertNotNull(savedLog.getId());
        assertEquals(1L, savedLog.getRequestId());
        assertEquals(RequestStatus.PENDING, savedLog.getOldStatus());
        assertEquals(RequestStatus.REPORTED, savedLog.getNewStatus());
        assertEquals(2L, savedLog.getTechnicianId());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    public void testFindByRequestIdOrderByTimestampDesc() {
        Long requestId = 1L;

        StatusLog log1 = new StatusLog(requestId, RequestStatus.PENDING, RequestStatus.REPORTED, 2L);

        StatusLog log2 = new StatusLog(requestId, RequestStatus.REPORTED, RequestStatus.ESTIMATED, 2L);

        StatusLog log3 = new StatusLog(requestId, RequestStatus.ESTIMATED, RequestStatus.ACCEPTED, 2L);

        StatusLog otherLog = new StatusLog(2L, RequestStatus.PENDING, RequestStatus.REPORTED, 3L);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.persist(otherLog);
        entityManager.flush();

        List<StatusLog> logs = statusLogRepository.findByRequestIdOrderByTimestampDesc(requestId);

        assertEquals(3, logs.size());
        assertEquals(RequestStatus.ACCEPTED, logs.get(0).getNewStatus()); // Most recent (log3)
        assertEquals(RequestStatus.ESTIMATED, logs.get(1).getNewStatus()); // Second most recent (log2)
        assertEquals(RequestStatus.REPORTED, logs.get(2).getNewStatus()); // Oldest (log1)
    }

    @Test
    public void testFindByTechnicianIdOrderByTimestampDesc() {
        Long technicianId = 2L;

        StatusLog log1 = new StatusLog(1L, RequestStatus.PENDING, RequestStatus.REPORTED, technicianId);

        StatusLog log2 = new StatusLog(2L, RequestStatus.PENDING, RequestStatus.REPORTED, technicianId);

        StatusLog log3 = new StatusLog(3L, RequestStatus.PENDING, RequestStatus.REPORTED, technicianId);

        StatusLog otherLog = new StatusLog(4L, RequestStatus.PENDING, RequestStatus.REPORTED, 3L);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.persist(otherLog);
        entityManager.flush();

        List<StatusLog> logs = statusLogRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);

        assertEquals(3, logs.size());
        assertEquals(3L, logs.get(0).getRequestId()); // Most recent (log3)
        assertEquals(2L, logs.get(1).getRequestId()); // Second most recent (log2)
        assertEquals(1L, logs.get(2).getRequestId()); // Oldest (log1)
    }

    @Test
    public void testFindAllStatusLogs() {
        StatusLog log1 = new StatusLog(1L, RequestStatus.PENDING, RequestStatus.REPORTED, 1L);
        StatusLog log2 = new StatusLog(2L, RequestStatus.PENDING, RequestStatus.REPORTED, 2L);
        StatusLog log3 = new StatusLog(3L, RequestStatus.REPORTED, RequestStatus.ESTIMATED, 1L);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.flush();

        List<StatusLog> allLogs = statusLogRepository.findAll();

        assertEquals(3, allLogs.size());
    }
}