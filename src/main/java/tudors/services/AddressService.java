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

@Log
@Service
public class AddressService {
    @Autowired
    private CSVReader csvReader;

    @Autowired
    private Solver solver;

    public List<AddressResponse> findSolutionByPostalCode(String postalCode) {
        try {
            return solver.searchPostalCode(postalCode, 3000);
        } catch (Exception e) {
            log.severe("Cannot find region by postal code!");
            return new ArrayList<>();
        }
    }

    private List<ScoreRegion> findRegions(List<String> inputFields, List<? extends Region> regions, List<Integer> scores, Integer extraScore) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        for (int i = 0; i < inputFields.size(); i++) {
            if (!inputFields.get(i).equals("")) {
                foundRegions.addAll(solver.findRegions(inputFields.get(i), regions, 1, scores.get(i) + extraScore));
            }
        }
        return foundRegions;
    }

    public List<AddressResponse> findSolutionByName(List<String> inputFields) {
        List<AddressResponse> addressResponses = new ArrayList<>();
        for (ScoreRegion region : findSolutions(inputFields)) {
            String cityName = WordUtils.capitalize(((City) region.getRegion()).getAsciiName());
            String stateName = WordUtils.capitalize(((City) region.getRegion()).getState().getAsciiName());
            String countryName = WordUtils.capitalize(((City) region.getRegion()).getState().getCountry().getAsciiName());
            Integer score = region.getScore();
            addressResponses.add(new AddressResponse(countryName, stateName, cityName));
        }
        return addressResponses;
    }

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
