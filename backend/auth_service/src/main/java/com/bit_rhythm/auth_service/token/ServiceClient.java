package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "auth_service", name = "service_clients")
@Getter
@Setter
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
    @Column(name = "scopes")
    private String scopes;

    /** Space-separated services this client may call (becomes the token "aud"), e.g. "orders-service payments-service". */
    @Column(name = "allowed_audiences")
    private String allowedAudiences;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
