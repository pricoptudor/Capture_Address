package tudors.administratives;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A decorator for the Region class,
 * adding a scoring functionality,
 * used to sort the results and return
 * those with higher score.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
@AllArgsConstructor
public class ScoreRegion implements Comparable<Integer> {
    private Region region;

    @EqualsAndHashCode.Exclude
    private Integer score;

    @Override
    public int compareTo(Integer o) {
        return score.compareTo(o);
    }
}
