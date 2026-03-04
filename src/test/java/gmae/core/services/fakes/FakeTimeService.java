package gmae.core.services.fakes;

import gmae.core.model.TimeView;
import gmae.core.services.GmaeTimeService;

import java.util.Map;
import java.util.Optional;

/**
 * Deterministic in-memory implementation of {@link GmaeTimeService}.
 *
 * <p>Starts at day 0, 00:00. Two hardcoded realms:</p>
 * <ul>
 *   <li><b>Shadowfen</b> — offset +120 minutes (+2 h)</li>
 *   <li><b>Crystalpeak</b> — offset −180 minutes (−3 h)</li>
 * </ul>
 */
public class FakeTimeService implements GmaeTimeService {

    private static final Map<String, Integer> REALM_OFFSETS = Map.of(
            "Shadowfen",   120,
            "Crystalpeak", -180
    );

    private long totalMinutes = 0;

    @Override
    public TimeView nowWorld() {
        return toTimeView(totalMinutes, null);
    }

    @Override
    public Optional<TimeView> toLocal(String realmName) {
        Integer offset = REALM_OFFSETS.get(realmName);
        if (offset == null) return Optional.empty();

        long localTotal = totalMinutes + offset;
        if (localTotal < 0) localTotal = 0;

        int days    = (int) (localTotal / (24 * 60));
        int hours   = (int) ((localTotal % (24 * 60)) / 60);
        int minutes = (int) (localTotal % 60);
        String localStr = String.format("Day %d %02d:%02d", days, hours, minutes);

        return Optional.of(toTimeView(totalMinutes, localStr));
    }

    @Override
    public void advanceMinutes(int minutes) {
        if (minutes <= 0) return;
        totalMinutes += minutes;
    }

    /** Exposed for tests that need to verify internal state. */
    public long getTotalMinutes() {
        return totalMinutes;
    }

    private static TimeView toTimeView(long total, String localStr) {
        int days    = (int) (total / (24 * 60));
        int hours   = (int) ((total % (24 * 60)) / 60);
        int minutes = (int) (total % 60);
        return new TimeView(days, hours, minutes, localStr);
    }
}
