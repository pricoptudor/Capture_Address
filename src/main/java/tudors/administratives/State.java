package tudors.administratives;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * State representation extending Region,
 * adding an extra asciiName field,
 * a base state (for states with alternate names)
 * a reference to the afferent country
 * and a list of cities.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class State extends Region {
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private State baseState;

    private String asciiName;

    private Country country;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<City> cityList;

    public State(String name, State baseState, Country country, String asciiName) {
        this.name = name;
        this.baseState = baseState;
        this.country = country;
        this.asciiName = asciiName;
        cityList = new ArrayList<>();
    }

    public void addCity(City city) {
        cityList.add(city);
    }
}