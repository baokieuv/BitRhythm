package com.bit_rhythm.noti_service.mapper;

import com.bit_rhythm.noti_service.dto.response.NotificationDto;
import com.bit_rhythm.noti_service.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Good practice for DTOs
)
public interface NotificationMapper {
    NotificationDto toDto(Notification notification);
}