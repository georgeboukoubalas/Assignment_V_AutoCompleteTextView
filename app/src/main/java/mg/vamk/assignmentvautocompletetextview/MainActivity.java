package mg.vamk.assignmentvautocompletetextview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SearchEvent;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // 1. Define four separate lists for the separate adapters
    private ArrayList<CatalogItem> listName = new ArrayList<>();
    private ArrayList<CatalogItem> listSurname = new ArrayList<>();
    private ArrayList<CatalogItem> listPhone = new ArrayList<>();
    private ArrayList<CatalogItem> listEdu = new ArrayList<>();
    private ArrayList<CatalogItem> listHobbies = new ArrayList<>();

    // 2. Define the four separate adapters
    private ArrayAdapter<CatalogItem> adapterName, adapterSurname, adapterPhone, adapterEdu, adapterHobbies;

    // 3. Define the Helper Method (Un-commented so it actually works)
    private void SearchSelect(AutoCompleteTextView searchItem, TextView TVresult) {
        searchItem.setOnItemClickListener((parent, view, position, id) -> {

            // 1. Get the custom object we created from the adapter
            CatalogItem selectedItem = (CatalogItem) parent.getItemAtPosition(position);

            // 2. Extract the hidden, full details string
            String cleanDisplay = selectedItem.fullDisplay;

            // 3. Show the full string in the bottom TextView
            TVresult.setText("USER DETAILS:\n" + cleanDisplay);

            // 4. Show the full string inside the search bar visually
            // 'false' is critical so it doesn't try to search the full string again
            searchItem.setText(cleanDisplay, false);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI References
        final EditText etFirst = findViewById(R.id.et_first_name);
        final EditText etLast = findViewById(R.id.et_last_name);
        final EditText etPhone = findViewById(R.id.et_phone);
        final Spinner spinnerEdu = findViewById(R.id.spinner_edu);
        final CheckBox cbSports = findViewById(R.id.cb_sports);
        final CheckBox cbReading = findViewById(R.id.cb_reading);
        final CheckBox cbMusic = findViewById(R.id.cb_music);

        final TextView tvResult = findViewById(R.id.tv_result);
        final AutoCompleteTextView actvSearchName = findViewById(R.id.actv_search_name);
        final AutoCompleteTextView actvSearchSurname = findViewById(R.id.actv_search_surname);
        final AutoCompleteTextView actvSearchPhone = findViewById(R.id.actv_search_phone);
        final AutoCompleteTextView actvSearchEducation = findViewById(R.id.actv_search_education);
        final AutoCompleteTextView actvHobbies = findViewById(R.id.actv_search_hobbies);
        Button btnSubmit = findViewById(R.id.btn_submit);

        // 1. Setup Education Spinner
        ArrayAdapter<CharSequence> eduSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.edu_levels, android.R.layout.simple_spinner_item);
        eduSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdu.setAdapter(eduSpinnerAdapter);

        // 2. Setup 5 separate AutoComplete Adapters
        adapterName = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listName);
        adapterSurname = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listSurname);
        adapterPhone = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listPhone);
        adapterEdu = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listEdu);
        adapterHobbies = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listHobbies);

        // Bind each specific bar to its specific adapter
        actvSearchName.setAdapter(adapterName);
        actvSearchSurname.setAdapter(adapterSurname);
        actvSearchPhone.setAdapter(adapterPhone);
        actvSearchEducation.setAdapter(adapterEdu);
        actvHobbies.setAdapter(adapterHobbies);

        // Set thresholds to 1 to show suggestions immediately
        actvSearchName.setThreshold(1);
        actvSearchSurname.setThreshold(1);
        actvSearchPhone.setThreshold(1);
        actvSearchEducation.setThreshold(1);
        actvHobbies.setThreshold(1);

        // 3. Submit Button Logic
        btnSubmit.setOnClickListener(v -> {
            String f = etFirst.getText().toString().trim();
            String l = etLast.getText().toString().trim();
            String p = etPhone.getText().toString().trim();
            String edu = spinnerEdu.getSelectedItem().toString();

            StringBuilder hobbies = new StringBuilder();
            if (cbSports.isChecked()) hobbies.append("Sports ");
            if (cbReading.isChecked()) hobbies.append("Reading ");
            if (cbMusic.isChecked()) hobbies.append("Music ");

            // Validation check
            if (!f.isEmpty() && !l.isEmpty() && !p.isEmpty()) {
                String h = hobbies.toString().trim();

                // This is the complete string we want to see visually
                String fullDisplay = f + " | " + l + " | " + p + " | " + edu + " | " + h;

                // Add to the specific adapter with the SearchKey at the START
                // Adding specific combinations to specific adapters as required
                adapterName.add(new CatalogItem(f, fullDisplay));
                adapterSurname.add(new CatalogItem(l, fullDisplay));
                adapterPhone.add(new CatalogItem(p, fullDisplay));
                adapterEdu.add(new CatalogItem(edu, fullDisplay));
                adapterHobbies.add(new CatalogItem(h, fullDisplay));

                Toast.makeText(this, "Data Saved to Catalog", Toast.LENGTH_SHORT).show();

                // Clear fields
                etFirst.setText(""); etLast.setText(""); etPhone.setText("");
                cbSports.setChecked(false); cbReading.setChecked(false); cbMusic.setChecked(false);
            } else {
                Toast.makeText(this, "Please fill Name, Last Name, and Phone", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Implement SearchSelect calls correctly for each bar
        SearchSelect(actvSearchName, tvResult);
        SearchSelect(actvSearchSurname, tvResult);
        SearchSelect(actvSearchPhone, tvResult);
        SearchSelect(actvSearchEducation, tvResult);
        SearchSelect(actvHobbies, tvResult);
    }
    // Helper object to keep search keys and display data separate
    public class CatalogItem {
        String searchKey;
        String fullDisplay;

        public CatalogItem(String searchKey, String fullDisplay) {
            this.searchKey = searchKey;
            this.fullDisplay = fullDisplay;
        }

        @Override
        public String toString() {
            // The AutoCompleteTextView uses this for searching AND the dropdown visual!
            return searchKey;
        }
    }
}