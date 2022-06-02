package tudors.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressResponse {
    private String country;
    private String state;
    private String city;
    private Integer score;
}
