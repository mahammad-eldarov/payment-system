package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusAuditLogMapper {
    StatusAuditLogResponse toResponse(StatusAuditLogEntity statusAuditLogEntity);
}
