package com.example.pos_driver.Model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table( name = "device_managers")
public class DeviceManager {
    @Id
    @Column(name = "device_id", nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID deviceId;
    //    @Column(name = "device_id", nullable = false)
//    private String  deviceId;
    @Column(name = "device_name", nullable = false)
    private String  deviceName;
    @Column(name = "device_description", nullable = false)
    private String  deviceDescription;
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

}
