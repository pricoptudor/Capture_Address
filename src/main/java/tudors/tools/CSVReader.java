package tudors.tools;

import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import tudors.administratives.*;
import tudors.solver.Solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pricop Tudor-Constantin 2A2
 * @author Tudose Tudor-Cristian 2A2
 * <p>
 * Class responsible with retrieving data from the input file(faster than database).
 * Globe -> structure that holds the structure of cities, states and cities.
 */
@Log
@Getter
@Component
public class CSVReader {
    private final Globe globe = new Globe();

    /**
     * Inserts data from an input file and initializes the globe(the whole structure) + sorts the resulted lists.
     */
    public void insertDataFromCsv(String path) {
        try (Scanner scanner = new Scanner(new File(path))) {
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> words = Arrays.stream(line.split(";")).toList();

                String cityNativeName = words.get(0).toLowerCase();
                String countryAsciiName = words.get(1).toLowerCase();
                String cityAsciiName = words.get(2).toLowerCase();
                String cityAlternateNames = words.get(3).toLowerCase();
                String countryCode2 = words.get(4).toLowerCase();
                String stateNativeName = words.get(5).toLowerCase();
                String stateAsciiName = words.get(6).toLowerCase();
                String countryAlternateNames = words.get(7).toLowerCase();
                String countryCode3 = words.get(8).toLowerCase();
                String countryIntCode = words.get(9).toLowerCase();

                Country country = new Country(countryAsciiName, null, countryAsciiName);
                country.setBaseCountry(country);

                Optional<Country> optionalCountry = globe.hasCountry(country);
                if (optionalCountry.isEmpty()) {
                    globe.addCountry(country);
                    globe.addCountry(new Country(countryCode2, country, countryAsciiName));
                    globe.addCountry(new Country(countryCode3, country, countryAsciiName));
                    globe.addCountry(new Country(countryIntCode, country, countryAsciiName));

                    if (!Objects.equals(countryAlternateNames, "null")) {
                        List<String> alternateNameList = Arrays.stream(countryAlternateNames.split(",")).toList();
                        for (String name : alternateNameList) {
                            if (Objects.equals(name, countryAsciiName)) continue;
                            globe.addCountry(new Country(name, country, countryAsciiName));
                        }
                    }

                } else {
                    country = optionalCountry.get();
                }

                State state = new State(stateAsciiName, null, country, stateAsciiName);
                state.setBaseState(state);

                Optional<State> optionalState = globe.hasState(state);
                if (optionalState.isEmpty()) {
                    country.addState(state);
                    globe.addState(state);
                    globe.addState(new State(stateNativeName, state, country, stateAsciiName));
                } else {
                    state = optionalState.get();
                }

                City city = new City(cityNativeName, cityAsciiName, state);
                state.addCity(city);
                country.addCity(city);
                globe.addCity(city);

                if (!Objects.equals(cityNativeName, cityAsciiName)) {
                    city = new City(cityAsciiName, cityAsciiName, state);
                    state.addCity(city);
                    country.addCity(city);
                    globe.addCity(city);
                }

                if (Objects.equals(cityAlternateNames, "null")) continue;

                List<String> alternateNameList = Arrays.stream(cityAlternateNames.split(",")).toList();
                for (String name : alternateNameList) {
                    if (Objects.equals(name, cityNativeName) || Objects.equals(name, cityAsciiName)) continue;
                    city = new City(name, cityAsciiName, state);
                    state.addCity(city);
                    country.addCity(city);
                    globe.addCity(city);
                }
            }
        } catch (FileNotFoundException e) {
            log.warning("File with path " + path + " not found!");
        }

        System.out.println("Initialized");
        globe.getCities().sort(Comparator.comparing(City::getName));
        globe.getStates().sort(Comparator.comparing(State::getName));
        globe.getCountries().sort(Comparator.comparing(Country::getName));
        for (Country country : globe.getCountries()) {
            country.getStateList().sort(Comparator.comparing(State::getName));
            country.getCityList().sort(Comparator.comparing(City::getName));
        }
        for (State state : globe.getStates()) {
            state.getCityList().sort(Comparator.comparing(City::getName));
        }
        System.out.println("Sorted: " + globe.getCities().size());
    }

    /**
     * Initialization of the program in-memory database.
     */
    public CSVReader() {
        insertDataFromCsv("src/main/resources/input.csv");
    }

    public static void main(String[] args) {
        //insertDataFromCsv("src/main/resources/input.csv");

        //Solver solver = new Solver();
        //solver.findRegions("romaniaaa", globe.getCountries(), 3, 10).forEach(System.out::println);

        //findRegions("Casin Onesti", globe.getCities(), 1, 10).forEach(System.out::println);
        //System.out.println(findRegions("Casin Onesti", globe.getCities(), 1, 10).size());
        //System.out.println(findRegions("Casin", globe.getCities(), 1, 10).size());
        //System.out.println(findRegions("276", globe.getCountries(), 4, 10));
    }
}
