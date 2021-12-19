package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid implements Serializable {

    private double bidValue; // the actual number (how much money are placed).
    private LocalTime timeBidPlaced;

}