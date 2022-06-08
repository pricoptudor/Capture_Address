package tudors.administratives;

import lombok.Data;

@Data
public class InputAddress {
    private String country;
    private String state;
    private String city;
    private String postalCode;
    private String streetLine;
}
