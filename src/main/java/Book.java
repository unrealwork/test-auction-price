import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@Getter
@ToString
public class Book {
    private final List<Order> bids;
    private final List<Order> asks;
}
