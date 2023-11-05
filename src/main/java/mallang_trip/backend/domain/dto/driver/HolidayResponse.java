package mallang_trip.backend.domain.dto.driver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private List<String> holidays;

    public static HolidayResponse ofWeek(List<DayOfWeek> weeks){
        List<String> holidays = weeks.stream()
            .map(DayOfWeek::toString)
            .collect(Collectors.toList());
        return HolidayResponse.builder()
            .holidays(holidays)
            .build();
    }

    public static HolidayResponse of(List<LocalDate> dates){
        List<String> holidays = dates.stream()
            .map(LocalDate::toString)
            .collect(Collectors.toList());
        return HolidayResponse.builder()
            .holidays(holidays)
            .build();
    }
}
