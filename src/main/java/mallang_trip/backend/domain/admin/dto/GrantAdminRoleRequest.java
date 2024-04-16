package mallang_trip.backend.domain.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrantAdminRoleRequest {

    @ApiModelProperty(value = "userIds List", required = true)
    List<Long> userIds;
}
