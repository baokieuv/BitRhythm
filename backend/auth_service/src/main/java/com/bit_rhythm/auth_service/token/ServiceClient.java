package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(schema = "auth_service", name = "service_clients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceClient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false)
    private String clientSecretHash;

    /** Space-separated scopes this client may request, e.g. "orders:read payments:write". */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "auth_service",
            name = "service_client_permissions",
            joinColumns = @JoinColumn(name = "service_client_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> scopes = new HashSet<>();

    /** Space-separated services this client may call (becomes the token "aud"), e.g. "orders-service payments-service". */
    @Column(name = "allowed_audiences")
    private String allowedAudiences;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
