package tudors.administratives;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
