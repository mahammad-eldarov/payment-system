package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.NotificationResponse;
import az.bank.paymentsystem.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(NotificationEntity notification);
}
