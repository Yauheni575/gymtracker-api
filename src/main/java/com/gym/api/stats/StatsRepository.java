package com.gym.api.stats;

import com.gym.api.entry.Entry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StatsRepository extends Repository<Entry, Long> {

    @Query("""
            select new com.gym.api.stats.RecordResponse(x.id, x.name, max(e.weight), count(e), max(e.date))
            from Entry e join e.exercise x
            where x.userId = :userId
            group by x.id, x.name
            order by x.name
            """)
    List<RecordResponse> records(@Param("userId") Long userId);

    @Query("""
            select new com.gym.api.stats.DayVolume(e.date, sum(e.weight * e.reps), count(e))
            from Entry e
            where e.exercise.userId = :userId and e.date between :from and :to
            group by e.date
            order by e.date
            """)
    List<DayVolume> volumeByDay(@Param("userId") Long userId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to);
}
