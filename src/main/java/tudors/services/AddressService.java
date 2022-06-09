package tudors.services;

import lombok.extern.java.Log;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tudors.administratives.*;
import tudors.dtos.AddressResponse;
import tudors.solver.Solver;
import tudors.tools.CSVReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service class handling the searching request
 * received in the AddressController class.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Log
@Service
public class AddressService {
    @Autowired
    private CSVReader csvReader;

    @Autowired
    private Solver solver;

    /**
     * Computes the results according to the postal code input.
     *
     * @param postalCode the input field corresponding to a city.
     * @return a list of addresses matching the given postal code.
     */
    public List<AddressResponse> findSolutionByPostalCode(String postalCode) {
        try {
            return solver.searchPostalCode(postalCode, 3000);
        } catch (Exception e) {
            log.severe("Cannot find region by postal code!");
            return new ArrayList<>();
        }
    }

    /**
     * Computes the results according to all fields given as input.
     * If an input field is empty, it is omitted.
     *
     * @param inputFields the given address fields.
     * @return a list of addresses matching the input address.
     */
    public List<AddressResponse> findSolutionByName(List<String> inputFields) {
        List<AddressResponse> addressResponses = new ArrayList<>();
        for (ScoreRegion region : findSolutions(inputFields)) {
            String cityName = WordUtils.capitalize(((City) region.getRegion()).getAsciiName());
            String stateName = WordUtils.capitalize(((City) region.getRegion()).getState().getAsciiName());
            String countryName = WordUtils.capitalize(((City) region.getRegion()).getState().getCountry().getAsciiName());
            addressResponses.add(new AddressResponse(countryName, stateName, cityName));
        }
        return addressResponses;
    }

    /**
     * Helper method which searches in a given list of regions,
     * trying to match the input fields with the regions in the list.
     *
     * @param inputFields the given input address fields.
     * @param regions     the region list to search in.
     * @param scores      the list of score according to the searching region.
     * @param extraScore  the extra score from a parent region previously found.
     * @return a list of matching regions, with their corresponding scores.
     */
    private List<ScoreRegion> findRegions(List<String> inputFields, List<? extends Region> regions, List<Integer> scores, Integer extraScore) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        for (int i = 0; i < inputFields.size(); i++) {
            if (!inputFields.get(i).equals("")) {
                foundRegions.addAll(solver.findRegions(inputFields.get(i), regions, 1, scores.get(i) + extraScore));
            }
        }
        return foundRegions;
    }

    /**
     * Helper method which makes a strategic searching,
     * starting with the countries, going to the states
     * and finally to the cities, in order to pass the
     * score of a parent region to its child.
     *
     * @param inputFields the given input address fields.
     * @return a list of matching regions, with their corresponding scores.
     */
    private List<ScoreRegion> findSolutions(List<String> inputFields) {
        List<ScoreRegion> countries;
        List<ScoreRegion> states = new ArrayList<>();
        List<ScoreRegion> cities = new ArrayList<>();

        countries = findRegions(inputFields, csvReader.getGlobe().getCountries(), Scores.COUNTRY_SCORES, 0);

        if (!countries.isEmpty()) {
            for (ScoreRegion country : countries) {
                states.addAll(findRegions(inputFields, ((Country) country.getRegion()).getBaseCountry().getStateList(), Scores.STATE_SCORES, country.getScore() / 10));
            }
            if (!states.isEmpty()) {
                for (ScoreRegion state : states) {
                    cities.addAll(findRegions(inputFields, ((State) state.getRegion()).getBaseState().getCityList(), Scores.CITY_SCORES, state.getScore() / 10));
                }
            } else {
                for (ScoreRegion country : countries) {
                    cities.addAll(findRegions(inputFields, ((Country) country.getRegion()).getBaseCountry().getCityList(), Scores.CITY_SCORES, country.getScore() / 10));
                }
            }
        }
        if (cities.isEmpty()) {
            cities.addAll(findRegions(inputFields, csvReader.getGlobe().getCities(), Scores.CITY_SCORES, 0));
        }

        cities.sort(Comparator.comparing(ScoreRegion::getScore).reversed());
        return cities;
    }
}
