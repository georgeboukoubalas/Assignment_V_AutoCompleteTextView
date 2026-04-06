package mg.vamk.assignmentvautocompletetextview;

/**
 * Represents a single catalog entry.
 * Static so it does NOT hold a reference to the outer Activity (prevents memory leaks).
 */
public class CatalogItem {

    public final String searchKey;   // The field this adapter searches by (name / surname / phone / edu / hobbies)
    public final String fullDisplay; // The complete pipe-separated record: "First|Last|Phone|Edu|Hobbies"

    public CatalogItem(String searchKey, String fullDisplay) {
        this.searchKey = searchKey;
        this.fullDisplay = fullDisplay;
    }

    /**
     * toString() is used by the default ArrayAdapter filter AND for the dropdown item text.
     * We return only the searchKey so the adapter's built-in filter stays scoped to this field.
     */
    @Override
    public String toString() {
        return searchKey;
    }
}
