package tudors.administratives;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Globe representation having lists of
 * countries, states and cities,
 * and methods to populate these lists.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
public class Globe {
    private List<Country> countries;
    private List<State> states;
    private List<City> cities;

    public Globe() {
        countries = new ArrayList<>();
        states = new ArrayList<>();
        cities = new ArrayList<>();
    }

    public void addCountry(Country country) {
        countries.add(country);
    }

    public void addState(State state) {
        states.add(state);
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public Optional<State> hasState(State state) {
        return states.stream().filter(state::equals).findFirst();
    }

    public Optional<Country> hasCountry(Country country) {
        return countries.stream().filter(country::equals).findFirst();
    }
}
