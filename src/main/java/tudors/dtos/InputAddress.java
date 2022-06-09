package tudors.dtos;

import lombok.Data;

/**
 * Dto class representing the input
 * of the problem, having 5 String fields
 * representing an address.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
public class InputAddress {
    private String country;
    private String state;
    private String city;
    private String postalCode;
    private String streetLine;
}
