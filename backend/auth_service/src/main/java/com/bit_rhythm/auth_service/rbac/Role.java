package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.rbac.dto.request.CreateRoleRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.UpdateRoleRequest;
import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(schema = "auth_service", name = "roles")
@Setter
@Getter
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "auth_service",
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role(CreateRoleRequest request) {
        BeanUtils.copyProperties(request, this);
        this.name = request.name().toUpperCase();
        this.description = request.description();
    }

    public void update(UpdateRoleRequest request) {
        BeanUtils.copyProperties(request, this);
        this.name = request.name().toUpperCase();
        this.description = request.description();
    }
}
