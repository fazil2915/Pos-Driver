package com.example.pos_driver.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "merchants")
public class Merchant {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @Column(name = "merchant_id", nullable = false, unique = true, length = 15)
    private String merchantId;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "card_group", nullable = true)
    private String cardGroup;

    @Column(name = "destination_group", nullable = true)
    private String destinationGroup;

    @Column(name = "merchant_type", nullable = true)
    private String merchantType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "acquiring_id", nullable = true)
    private String acquiringId;

    @Column(name = "forwarding_id", nullable = true)
    private String forwardingId;

    @Column(name = "location", nullable = true)
    private String location;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "state", nullable = true)
    private String state;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "created_date", nullable = true)
    private LocalDateTime createdDate;

}
