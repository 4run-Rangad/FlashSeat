package com.flashseat.flashseat_backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingSeatId implements Serializable {

    private Long bookingId;

    private Long seatId;

}
