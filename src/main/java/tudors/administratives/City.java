package tudors.administratives;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * City representation extending Region,
 * adding an extra asciiName property
 * and a state.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class City extends Region {
    private String asciiName;
    private State state;

    public City(String name, String asciiName, State state) {
        this.name = name;
        this.asciiName = asciiName;
        this.state = state;
    }
}
