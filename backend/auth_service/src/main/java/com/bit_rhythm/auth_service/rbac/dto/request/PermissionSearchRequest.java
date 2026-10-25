package com.bit_rhythm.auth_service.rbac.dto.request;

import com.bit_rhythm.core.dto.BaseSearchRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionSearchRequest extends BaseSearchRequest {
    private String name;
    private String description;
}
