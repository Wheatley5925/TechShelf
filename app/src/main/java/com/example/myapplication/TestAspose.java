import com.aspose.words.Document;

public class TestAspose {
    public void test() {
        try {
            Document doc = new Document();
            doc.save("test.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
