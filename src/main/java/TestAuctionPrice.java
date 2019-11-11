import java.util.List;
import java.util.Optional;

public class TestAuctionPrice {
    public static void main(String[] args) {
        final AuctionPriceCalculator calculator = new AuctionPriceCalculator();
        List<Book> books = Optional.ofNullable(TestAuctionPrice.class.getClassLoader().getResource("test.in"))
                .map(FileUtils::readBooks)
                .orElseThrow(IllegalStateException::new);
        books.forEach(calculator::calc);
        System.out.println(books);
    }
}
