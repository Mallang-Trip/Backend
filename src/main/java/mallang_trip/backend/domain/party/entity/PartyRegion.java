package mallang_trip.backend.domain.party.entity;

import lombok.*;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

//가고 싶은 지역
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE party_region SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class PartyRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String region;

    @Column
    @Builder.Default()
    private Integer driverCount=0; // 해당 지역에 대한 파티 수

    @Column(name="region_img", nullable = false)
    private String regionImg;

    public void addCount(){
        this.driverCount++;
    }

    public void subCount(){
        this.driverCount--;
    }

    public Boolean isZero(){
        return this.driverCount==0;
    }

    public void modify(String region, String regionImg){
        this.region = region;
        this.regionImg = regionImg;
    }
}
