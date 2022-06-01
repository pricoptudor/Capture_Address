package tudors.administratives;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class Country extends Region {
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Country baseCountry;

    private String asciiName;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<State> stateList;


    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<City> cityList;


    public Country(String name, Country baseCountry, String asciiName) {
        this.name = name;
        this.baseCountry = baseCountry;
        this.asciiName = asciiName;
        stateList = new ArrayList<>();
        cityList = new ArrayList<>();
    }

    public void addState(State state) {
        stateList.add(state);
    }

    public void addCity(City city) {
        cityList.add(city);
    }
}
