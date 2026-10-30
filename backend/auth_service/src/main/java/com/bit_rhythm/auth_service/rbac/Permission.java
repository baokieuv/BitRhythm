package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.rbac.dto.request.CreatePermissionRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.UpdatePermissionRequest;
import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Entity
@Table(schema = "auth_service", name = "permissions")
@Getter
@Setter
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    public Permission(CreatePermissionRequest request) {
        BeanUtils.copyProperties(request, this);
        this.name = request.name().toUpperCase();
        this.description = request.description();
    }

    public void update(UpdatePermissionRequest request) {
        BeanUtils.copyProperties(request, this);
        this.name = request.name().toUpperCase();
        this.description = request.description();
    }
}
