package mallang_trip.backend.domain.driver.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.driver.dto.AdminDriverProfileRequest;
import mallang_trip.backend.domain.driver.dto.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationRequest;
import mallang_trip.backend.global.entity.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE driver SET deleted = true WHERE id = ?")
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

    public void changeRegistration(DriverRegistrationRequest request) {
        this.vehicleModel = request.getVehicleModel();
        this.vehicleCapacity = request.getVehicleCapacity();
        this.vehicleNumber = request.getVehicleNumber();
        this.vehicleImg = request.getVehicleImg();
        this.region = request.getRegion();
        this.bank = request.getBank();
        this.accountHolder = request.getAccountHolder();
        this.accountNumber = request.getAccountNumber();
        this.driverLicenceImg = request.getDriverLicenceImg();
        this.taxiLicenceImg = request.getTaxiLicenceImg();
        this.insuranceLicenceImg = request.getInsuranceLicenceImg();
        this.introduction = request.getIntroduction();
        this.status = DriverStatus.WAITING;
    }

    public void changeProfile(ChangeDriverProfileRequest request) {
        this.region = request.getRegion();
        this.bank = request.getBank();
        this.accountHolder = request.getAccountHolder();
        this.accountNumber = request.getAccountNumber();
        this.vehicleImg = request.getVehicleImg();
        this.vehicleModel = request.getVehicleModel();
        this.vehicleNumber = request.getVehicleNumber();
        this.vehicleCapacity = request.getVehicleCapacity();
        this.introduction = request.getIntroduction();
        changeHoliday(request.getHolidays());
        changeWeeklyHoliday(request.getWeeklyHolidays());
    }

    public void changeProfileByAdmin(AdminDriverProfileRequest request) {
        this.region = request.getRegion();
        this.bank = request.getBank();
        this.accountHolder = request.getAccountHolder();
        this.accountNumber = request.getAccountNumber();
        this.vehicleImg = request.getVehicleImg();
        this.vehicleModel = request.getVehicleModel();
        this.vehicleNumber = request.getVehicleNumber();
        this.vehicleCapacity = request.getVehicleCapacity();
        this.introduction = request.getIntroduction();
        this.driverLicenceImg = request.getDriverLicenseImg();
        this.taxiLicenceImg = request.getTaxiLicenseImg();
        this.insuranceLicenceImg = request.getInsuranceLicenseImg();
        changeHoliday(request.getHolidays());
        changeWeeklyHoliday(request.getWeeklyHolidays());
    }

    public void changeStatus(DriverStatus status) {
        this.status = status;
    }

    private void changeWeeklyHoliday(List<String> holiday) {
        this.weeklyHoliday = holiday.stream()
            .map(DayOfWeek::valueOf)
            .collect(Collectors.toList());
    }

    private void changeHoliday(List<String> holiday) {
        this.holiday = holiday.stream()
            .map(LocalDate::parse)
            .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Driver driver = (Driver) o;
        return Objects.equals(id, driver.getId());
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.intValue() : 0;
        return result;
    }
}
