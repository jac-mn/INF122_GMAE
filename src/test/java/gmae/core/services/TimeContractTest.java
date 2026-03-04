package gmae.core.services;

import gmae.core.model.TimeView;
import gmae.core.services.fakes.FakeTimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for {@link GmaeTimeService}.
 *
 * <p>Run against the {@link FakeTimeService} reference implementation.
 * Sub-team #2 can copy this class, swap the implementation in
 * {@link #createService()}, and verify their adapter passes the same
 * invariants.</p>
 */
public class TimeContractTest {

    private GmaeTimeService svc;

    /** Override this method to test a different implementation. */
    protected GmaeTimeService createService() {
        return new FakeTimeService();
    }

    @BeforeEach
    void setUp() {
        svc = createService();
    }

    // ── initial state ──────────────────────────────────────────

    @Test
    void initialTime_isZero() {
        TimeView t = svc.nowWorld();
        assertEquals(0, t.days());
        assertEquals(0, t.hours());
        assertEquals(0, t.minutes());
    }

    // ── advanceMinutes ─────────────────────────────────────────

    @Test
    void advanceMinutes_advancesExactly() {
        svc.advanceMinutes(42);

        TimeView t = svc.nowWorld();
        assertEquals(0, t.days());
        assertEquals(0, t.hours());
        assertEquals(42, t.minutes());
    }

    @Test
    void advanceMinutes_rolloverMinutesToHours() {
        svc.advanceMinutes(90);

        TimeView t = svc.nowWorld();
        assertEquals(0, t.days());
        assertEquals(1, t.hours());
        assertEquals(30, t.minutes());
    }

    @Test
    void advanceMinutes_rolloverHoursToDays() {
        svc.advanceMinutes(24 * 60 + 30);

        TimeView t = svc.nowWorld();
        assertEquals(1, t.days());
        assertEquals(0, t.hours());
        assertEquals(30, t.minutes());
    }

    @Test
    void advanceMinutes_multipleCallsAccumulate() {
        svc.advanceMinutes(60);
        svc.advanceMinutes(60);
        svc.advanceMinutes(60);

        TimeView t = svc.nowWorld();
        assertEquals(3 * 60, t.toTotalMinutes());
        assertEquals(3, t.hours());
    }

    @Test
    void advanceMinutes_largeValue() {
        svc.advanceMinutes(3 * 24 * 60 + 5 * 60 + 17);

        TimeView t = svc.nowWorld();
        assertEquals(3, t.days());
        assertEquals(5, t.hours());
        assertEquals(17, t.minutes());
    }

    // ── toLocal ────────────────────────────────────────────────

    @Test
    void toLocal_unknownRealm_returnsEmpty() {
        Optional<TimeView> result = svc.toLocal("Nonexistent Realm");
        assertTrue(result.isEmpty());
    }

    @Test
    void toLocal_knownRealm_returnsConvertedTime() {
        svc.advanceMinutes(60);

        Optional<TimeView> result = svc.toLocal("Shadowfen");
        assertTrue(result.isPresent());

        TimeView tv = result.get();
        assertNotNull(tv.localTimeString());
        assertEquals("Day 0 03:00", tv.localTimeString());
    }

    @Test
    void toLocal_knownRealmNegativeOffset_clampsOrWraps() {
        Optional<TimeView> result = svc.toLocal("Crystalpeak");
        assertTrue(result.isPresent());

        TimeView tv = result.get();
        assertNotNull(tv.localTimeString());
    }

    @Test
    void toLocal_worldFieldsUnchanged() {
        svc.advanceMinutes(90);

        Optional<TimeView> result = svc.toLocal("Shadowfen");
        assertTrue(result.isPresent());

        TimeView tv = result.get();
        assertEquals(0, tv.days());
        assertEquals(1, tv.hours());
        assertEquals(30, tv.minutes());
    }
}
