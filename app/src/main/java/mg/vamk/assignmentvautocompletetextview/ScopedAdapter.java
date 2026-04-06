package mg.vamk.assignmentvautocompletetextview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom ArrayAdapter<CatalogItem> that:
 *
 *  • Filters suggestions using ONLY the item's searchKey  →  the adapter stays
 *    "scoped" (name adapter never matches on phone numbers, etc.).
 *
 *  • Renders each dropdown row as the FULL display string
 *    (e.g. "John|Doe|040123|University|Sports") so the user sees everything.
 *
 *  • When an item is selected, SearchSelect() puts fullDisplay into the text
 *    field with setText(..., false) so no second filtering pass is triggered.
 */
public class ScopedAdapter extends ArrayAdapter<CatalogItem> {

    // Master list — every CatalogItem ever added to this adapter
    private final List<CatalogItem> allItems = new ArrayList<>();
    // Subset shown after the current filter pass
    private final List<CatalogItem> filteredItems = new ArrayList<>();

    private final ScopedFilter scopedFilter = new ScopedFilter();

    public ScopedAdapter(Context context) {
        // We manage our own list; pass an empty list to super
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
    }

    // ── Override add() so we keep allItems in sync ────────────────────────────

    @Override
    public void add(@Nullable CatalogItem item) {
        if (item != null) {
            allItems.add(item);
            filteredItems.add(item); // also visible until user types something
        }
        notifyDataSetChanged();
    }

    // ── Adapter data source uses filteredItems ────────────────────────────────

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Nullable
    @Override
    public CatalogItem getItem(int position) {
        return filteredItems.get(position);
    }

    // ── Render each row as the fullDisplay string ─────────────────────────────

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        CatalogItem item = filteredItems.get(position);
        ((TextView) convertView.findViewById(android.R.id.text1))
                .setText(item != null ? item.fullDisplay : "");
        return convertView;
    }

    // ── Hand the custom filter to AutoCompleteTextView ────────────────────────

    @NonNull
    @Override
    public Filter getFilter() {
        return scopedFilter;
    }

    // ── Custom filter: match on searchKey only ────────────────────────────────

    private class ScopedFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<CatalogItem> matched = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // No query → show everything
                matched.addAll(allItems);
            } else {
                String query = constraint.toString().toLowerCase().trim();
                for (CatalogItem item : allItems) {
                    // Compare ONLY against the searchKey — fullDisplay is never touched here
                    if (item.searchKey.toLowerCase().contains(query)) {
                        matched.add(item);
                    }
                }
            }

            results.values = matched;
            results.count  = matched.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems.clear();
            if (results.values != null) {
                filteredItems.addAll((List<CatalogItem>) results.values);
            }
            if (filteredItems.isEmpty()) {
                notifyDataSetInvalidated();
            } else {
                notifyDataSetChanged();
            }
        }

        /**
         * Called by AutoCompleteTextView to decide what text to put in the field
         * AFTER the user picks an item from the dropdown.
         *
         * We return the searchKey so the field shows just the matched value
         * (MainActivity.SearchSelect then immediately replaces it with fullDisplay
         * using setText(..., false), which suppresses a second filter pass).
         */
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue instanceof CatalogItem) {
                return ((CatalogItem) resultValue).searchKey;
            }
            return super.convertResultToString(resultValue);
        }
    }
}
