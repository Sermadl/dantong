package org.jenga.dantong.user.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DkuInfoRequest {

    @NotBlank
    private String dkuId;
    @NotBlank
    private String dkuPassword;
}
