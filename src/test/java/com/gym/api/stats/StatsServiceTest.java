package com.gym.api.stats;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsServiceTest {

    private final StatsRepository repository = mock(StatsRepository.class);
    private final StatsService service = new StatsService(repository);

    @Test
    void sumsVolumeAndSetsOverDays() {
        LocalDate from = LocalDate.of(2026, 11, 10);
        LocalDate to = LocalDate.of(2026, 11, 14);
        when(repository.volumeByDay(1L, from, to)).thenReturn(List.of(
                new DayVolume(from, 800.0, 2L),
                new DayVolume(to, 330.0, 1L)));

        VolumeResponse result = service.volume(1L, from, to);

        assertThat(result.total()).isEqualTo(1130.0);
        assertThat(result.sets()).isEqualTo(3);
        assertThat(result.days()).hasSize(2);
    }

    @Test
    void defaultsToLastSevenDays() {
        LocalDate today = LocalDate.now();
        when(repository.volumeByDay(any(), any(), any())).thenReturn(List.of());

        VolumeResponse result = service.volume(1L, null, null);

        assertThat(result.from()).isEqualTo(today.minusDays(6));
        assertThat(result.to()).isEqualTo(today);
        verify(repository).volumeByDay(1L, today.minusDays(6), today);
    }

    @Test
    void rejectsFromAfterTo() {
        assertThatThrownBy(() -> service.volume(1L, LocalDate.of(2026, 11, 14), LocalDate.of(2026, 11, 10)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(repository, never()).volumeByDay(any(), any(), any());
    }

    @Test
    void rejectsPeriodLongerThanYear() {
        assertThatThrownBy(() -> service.volume(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
