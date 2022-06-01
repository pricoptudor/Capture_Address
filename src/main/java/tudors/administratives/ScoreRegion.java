package tudors.administratives;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
