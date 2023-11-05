package mallang_trip.backend.domain.entity.driver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.domain.entity.BaseEntity;
import mallang_trip.backend.domain.entity.user.User;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Driver extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @Column(name = "vehicle_img", nullable = false)
    private String vehicleImg;

    @Column(name = "driver_license_img", nullable = false)
    private String driverLicenceImg;

    @Column(name = "taxi_license_img", nullable = false)
    private String taxiLicenceImg;

    @Column(name = "insurance_license_img", nullable = false)
    private String insuranceLicenceImg;

    @Column(name = "vehicle_model", nullable = false)
    private String vehicleModel;

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Column(name = "vehicle_capacity", nullable = false)
    private Integer vehicleCapacity;

    @Column
    private String region;

    @Column
    private String bank;

    @Column
    private String accountHolder;

    @Column
    private String accountNumber;

    @Column
    private String introduction;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> weeklyHoliday;

    @ElementCollection
    private List<LocalDate> holiday;

    @Enumerated(EnumType.STRING)
    @Builder.Default()
    private DriverStatus status = DriverStatus.WAITING;
}
