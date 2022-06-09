package tudors.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Dto class representing the output,
 * the computed result of the problem,
 * having the 3 corrected fields
 * (country, state, city).
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
@AllArgsConstructor
public class AddressResponse {
    private String country;
    private String state;
    private String city;
}
