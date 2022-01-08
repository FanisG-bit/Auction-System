package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalTime;

/** Represents a Bid.
 *  @author Theofanis Gkoufas
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid implements Serializable {

    /** The actual number (how much money is placed by the participant-user).
     */
    private double bidValue;
    /** Stores the server-side time in which the bid was placed.
     */
    private LocalTime timeBidPlaced;

}