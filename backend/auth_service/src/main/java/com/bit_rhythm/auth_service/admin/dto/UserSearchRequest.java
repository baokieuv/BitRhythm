package com.bit_rhythm.auth_service.admin.dto;

import com.bit_rhythm.core.dto.BaseSearchRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequest extends BaseSearchRequest {
    private String query;
    private String status;
}
