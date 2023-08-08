package mallang_trip.backend.domain.dto.driver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.constant.Week;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private List<String> holidays;

    public static HolidayResponse ofWeek(List<Week> weeks){
        List<String> holidays = new ArrayList<>();
        for(Week week : weeks){
            holidays.add(week.toString());
        }
        return HolidayResponse.builder()
            .holidays(holidays)
            .build();
    }

    public static HolidayResponse of(List<LocalDate> dates){
        List<String> holidays = new ArrayList<>();
        for(LocalDate date : dates){
            holidays.add(date.toString());
        }
        return HolidayResponse.builder()
            .holidays(holidays)
            .build();
    }
}
