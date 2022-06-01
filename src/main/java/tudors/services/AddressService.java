package tudors.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tudors.administratives.*;
import tudors.solver.Solver;
import tudors.tools.CSVReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class AddressService {
    @Autowired
    private CSVReader csvReader;

    @Autowired
    private Solver solver;

    private List<ScoreRegion> findRegions(List<String> inputFields, List<? extends Region> regions, List<Integer> scores, Integer extraScore) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        for (int i = 0; i < inputFields.size(); i++) {
            if (!inputFields.get(i).equals("")) {
                foundRegions.addAll(solver.findRegions(inputFields.get(i), regions, 1, scores.get(i) + extraScore));
            }
        }
        return foundRegions;
    }

    public List<ScoreRegion> findSolutions(List<String> inputFields) {
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
        } else {
            cities.addAll(findRegions(inputFields, csvReader.getGlobe().getCities(), Scores.CITY_SCORES, 0));
        }

        cities.sort(Comparator.comparing(ScoreRegion::getScore).reversed());
        return cities;
    }
}
