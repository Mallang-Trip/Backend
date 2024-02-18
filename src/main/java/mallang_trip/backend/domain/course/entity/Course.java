package mallang_trip.backend.domain.course.entity;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.domain.global.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE course SET deleted = true WHERE id = ?")
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ElementCollection
    @OrderColumn
    private List<String> images;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column
    private String name;

    @Column
    private Integer capacity;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Column(name = "discount_price")
    @Builder.Default()
    private Integer discountPrice = 0;

    public void increaseDiscountPrice(Integer amount){
        this.discountPrice += amount;
    }
}