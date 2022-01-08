package org.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/** Represents an Item.
 *  @author Theofanis Gkoufas
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item implements Serializable {

    private String itemName;
    private String itemDescription;
    private double itemStartingPrice;

}