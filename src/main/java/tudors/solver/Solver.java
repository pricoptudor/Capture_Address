package tudors.solver;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.geonames.*;
import org.springframework.stereotype.Component;
import tudors.administratives.Country;
import tudors.administratives.Region;
import tudors.administratives.ScoreRegion;
import tudors.administratives.State;
import tudors.model.AddressResponse;
import tudors.tools.CSVReader;

import java.util.*;

@Component
public class Solver {

    private List<ScoreRegion> scoreRegions = new ArrayList<>();

    //webservice for searching postal codes:
    public List<AddressResponse> searchPostalCode(String postalCode, Integer score) throws Exception {
        List<AddressResponse> found = new ArrayList<>();

        WebService.setUserName("tudor007"); // add your username here

        ToponymSearchCriteria toponymSearchCriteria = new ToponymSearchCriteria();
        toponymSearchCriteria.setStyle(Style.FULL);
        toponymSearchCriteria.setFeatureClass(FeatureClass.P);

        PostalCodeSearchCriteria searchCriteria = new PostalCodeSearchCriteria();
        searchCriteria.setStyle(Style.FULL);
        searchCriteria.setPostalCode(postalCode);

        WebService.postalCodeSearch(searchCriteria);
        List<PostalCode> searchResult = WebService.postalCodeSearch(searchCriteria);

        for(PostalCode code:searchResult){
            //System.out.println(code.getPlaceName()+" : "+code.getCountryCode()+" : "+code.getAdminCode1());

            toponymSearchCriteria.setAdminCode1(code.getAdminCode1());
            toponymSearchCriteria.setCountryCode(code.getCountryCode());
            toponymSearchCriteria.setNameEquals(code.getPlaceName());

            ToponymSearchResult toponyms = WebService.search(toponymSearchCriteria);
            for(Toponym toponym:toponyms.getToponyms()){
                found.add(new AddressResponse(toponym.getCountryName(),toponym.getAdminName1(),toponym.getName(),score));
                //System.out.println(toponym.getName()+" : "+toponym.getAdminName1()+" : "+toponym.getCountryName());
            }
        }
        return found;
    }

    private void addOrReplace(ScoreRegion currentRegion) {
        boolean found = false;
        for (ScoreRegion region : scoreRegions) {
            if (currentRegion.getRegion().equals(region.getRegion())) {
                if (currentRegion.getScore() > region.getScore()) {
                    region.setScore(currentRegion.getScore());
                }
                found = true;
            }
        }
        if (!found) {
            scoreRegions.add(currentRegion);
        }
    }

    public List<ScoreRegion> findRegions(String input, List<? extends Region> list, int distance, int score) {
        scoreRegions = new ArrayList<>();
        List<ScoreRegion> leetRegions = findFormattedRegions(leetFormattedInput(input).toLowerCase(), list, distance, score);
        List<ScoreRegion> formattedRegions = findFormattedRegions(input.toLowerCase(), list, distance, score);
        leetRegions.forEach(this::addOrReplace);
        formattedRegions.forEach(this::addOrReplace);
        scoreRegions.sort(Comparator.comparing(ScoreRegion::getScore).reversed());
        return scoreRegions;
    }

    private List<ScoreRegion> findFormattedRegions(String input, List<? extends Region> list, int distance, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        foundRegions.addAll(findBinaryRegions(input, list, score * 10));
        foundRegions.addAll(findTokenizedRegions(input, list, distance, score * 4));
        foundRegions.addAll(findEditDistanceRegions(input, list, distance, score * 2));

        return foundRegions;
    }

    private List<ScoreRegion> findTokenizedRegions(String input, List<? extends Region> list, int distance, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(input, ",.-/ ");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            foundRegions.addAll(findEditDistanceRegions(name, list, distance, score));
        }
        return foundRegions;
    }

    private List<ScoreRegion> findContainedRegions(String input, List<? extends Region> list, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        for (Region region : list) {
            if (input.contains(region.getName())) {
                foundRegions.add(new ScoreRegion(region, score));
            }
        }
        return foundRegions;
    }

    private List<ScoreRegion> findEditDistanceRegions(String input, List<? extends Region> list, int distance, int score) {
        LevenshteinDistance levDist = new LevenshteinDistance();
        List<ScoreRegion> foundRegions = new ArrayList<>();
        int currentDistance;
        for (Region region : list) {
            currentDistance = levDist.apply(region.getName(), input);
            if (currentDistance <= distance) {
                foundRegions.add(new ScoreRegion(region, score / (currentDistance + 1)));
            }
        }
        return foundRegions;
    }

    private List<ScoreRegion> findBinaryRegions(String input, List<? extends Region> list, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        int startIndex = Collections.binarySearch(list, input);
        if (startIndex < 0) return foundRegions;
        int listSize = list.size();
        for (int i = startIndex - 1; i >= 0 && list.get(i).getName().equals(input); i--) {
            foundRegions.add(new ScoreRegion(list.get(i), score));
        }
        for (int i = startIndex; i < listSize && list.get(i).getName().equals(input); i++) {
            foundRegions.add(new ScoreRegion(list.get(i), score));
        }
        return foundRegions;
    }

    private String leetFormattedInput(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '0' -> builder.append('o');
                case '1' -> builder.append('i');
                case '2' -> builder.append('r');
                case '3' -> builder.append('e');
                case '4' -> builder.append('a');
                case '5' -> builder.append('s');
                case '6', '8' -> builder.append('b');
                case '7' -> builder.append('t');
                case '9' -> builder.append('g');
                default -> builder.append(input.charAt(i));
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Solver solver = new Solver();
        CSVReader csvReader = new CSVReader();

        List<ScoreRegion> countries = solver.findRegions("Romania", csvReader.getGlobe().getCountries(), 1, 10);
        List<ScoreRegion> states = new ArrayList<>();
        List<ScoreRegion> cities = new ArrayList<>();

        if (countries.size() > 0) {
            for (ScoreRegion country : countries) {
                solver.findRegions("Bacau", ((Country) country.getRegion()).getBaseCountry().getStateList(), 1, 10);
                states.addAll(solver.findRegions("Bacau", ((Country) country.getRegion()).getBaseCountry().getStateList(), 1, 10));
            }

            if (states.size() > 0) {
                for (ScoreRegion state : states) {
                    solver.findRegions("Onesti", ((State) state.getRegion()).getBaseState().getCityList(), 1, 10)
                            .forEach(System.out::println);
                }
            }

            cities.forEach(System.out::println);
            System.out.println("--------------------------------------");

            solver.findRegions("Onesti", csvReader.getGlobe().getCities(), 1, 10).forEach(System.out::println);
        }
    }
}
