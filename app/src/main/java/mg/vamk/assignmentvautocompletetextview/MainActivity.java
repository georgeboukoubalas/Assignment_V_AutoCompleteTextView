package mg.vamk.assignmentvautocompletetextview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ── Five scoped adapters, one per searchable field ────────────────────────
    private ScopedAdapter adapterName;
    private ScopedAdapter adapterSurname;
    private ScopedAdapter adapterPhone;
    private ScopedAdapter adapterEdu;
    private ScopedAdapter adapterHobbies;

    // ── Persistent storage ────────────────────────────────────────────────────
    private DataRepository repository;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialise repository
        repository = new DataRepository(this);

        // 2. Bind UI references
        final EditText etFirst    = findViewById(R.id.et_first_name);
        final EditText etLast     = findViewById(R.id.et_last_name);
        final EditText etPhone    = findViewById(R.id.et_phone);
        final Spinner  spinnerEdu = findViewById(R.id.spinner_edu);
        final CheckBox cbSports   = findViewById(R.id.cb_sports);
        final CheckBox cbReading  = findViewById(R.id.cb_reading);
        final CheckBox cbMusic    = findViewById(R.id.cb_music);
        final TextView tvResult   = findViewById(R.id.tv_result);
        final Button   btnSubmit  = findViewById(R.id.btn_submit);

        final AutoCompleteTextView actvName      = findViewById(R.id.actv_search_name);
        final AutoCompleteTextView actvSurname   = findViewById(R.id.actv_search_surname);
        final AutoCompleteTextView actvPhone     = findViewById(R.id.actv_search_phone);
        final AutoCompleteTextView actvEducation = findViewById(R.id.actv_search_education);
        final AutoCompleteTextView actvHobbies   = findViewById(R.id.actv_search_hobbies);

        // 3. Education spinner
        ArrayAdapter<CharSequence> eduSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.edu_levels, android.R.layout.simple_spinner_item);
        eduSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdu.setAdapter(eduSpinnerAdapter);

        // 4. Build the five ScopedAdapters
        adapterName    = new ScopedAdapter(this);
        adapterSurname = new ScopedAdapter(this);
        adapterPhone   = new ScopedAdapter(this);
        adapterEdu     = new ScopedAdapter(this);
        adapterHobbies = new ScopedAdapter(this);

        // 5. Bind each AutoCompleteTextView to its adapter
        actvName.setAdapter(adapterName);
        actvSurname.setAdapter(adapterSurname);
        actvPhone.setAdapter(adapterPhone);
        actvEducation.setAdapter(adapterEdu);
        actvHobbies.setAdapter(adapterHobbies);

        // threshold=1 → suggestions appear after the first character
        actvName.setThreshold(1);
        actvSurname.setThreshold(1);
        actvPhone.setThreshold(1);
        actvEducation.setThreshold(1);
        actvHobbies.setThreshold(1);

        // 6. Populate adapters from persisted data (restore previous sessions)
        loadSavedSubmissionsIntoAdapters();

        // 7. Wire up selection listeners
        bindSearchSelect(actvName,      tvResult);
        bindSearchSelect(actvSurname,   tvResult);
        bindSearchSelect(actvPhone,     tvResult);
        bindSearchSelect(actvEducation, tvResult);
        bindSearchSelect(actvHobbies,   tvResult);

        // 8. Submit button
        btnSubmit.setOnClickListener(v -> {

            String first  = etFirst.getText().toString().trim();
            String last   = etLast.getText().toString().trim();
            String phone  = etPhone.getText().toString().trim();
            String edu    = spinnerEdu.getSelectedItem().toString();

            // Build hobbies string from checked boxes
            StringBuilder sb = new StringBuilder();
            if (cbSports.isChecked())  sb.append("Sports ");
            if (cbReading.isChecked()) sb.append("Reading ");
            if (cbMusic.isChecked())   sb.append("Music ");
            String hobbies = sb.toString().trim();

            // Basic validation
            if (first.isEmpty() || last.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this,
                        "Please fill Name, Last Name, and Phone",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ── Persist to disk ───────────────────────────────────────────
            repository.save(first, last, phone, edu, hobbies);

            // ── Build the shared full-display string ──────────────────────
            String fullDisplay = first + "|" + last + "|" + phone + "|" + edu + "|" + hobbies;

            // ── Push one CatalogItem into each scoped adapter ─────────────
            //    Each item's searchKey is the field that adapter is responsible for.
            //    fullDisplay is the same across all five — only the searchKey differs.
            adapterName.add(new CatalogItem(first,   fullDisplay));
            adapterSurname.add(new CatalogItem(last,    fullDisplay));
            adapterPhone.add(new CatalogItem(phone,  fullDisplay));
            adapterEdu.add(new CatalogItem(edu,    fullDisplay));
            adapterHobbies.add(new CatalogItem(hobbies, fullDisplay));

            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();

            // ── Clear input fields ────────────────────────────────────────
            etFirst.setText("");
            etLast.setText("");
            etPhone.setText("");
            cbSports.setChecked(false);
            cbReading.setChecked(false);
            cbMusic.setChecked(false);
        });
    }

    // ── Helper: load persisted records back into all adapters on app start ────

    private void loadSavedSubmissionsIntoAdapters() {
        List<DataRepository.Submission> saved = repository.loadAll();
        for (DataRepository.Submission s : saved) {
            String fullDisplay = s.toFullDisplay();
            adapterName.add(new CatalogItem(s.first,   fullDisplay));
            adapterSurname.add(new CatalogItem(s.last,    fullDisplay));
            adapterPhone.add(new CatalogItem(s.phone,  fullDisplay));
            adapterEdu.add(new CatalogItem(s.edu,    fullDisplay));
            adapterHobbies.add(new CatalogItem(s.hobbies, fullDisplay));
        }
    }

    // ── Helper: wire up item-click listener for one AutoCompleteTextView ──────

    /**
     * When the user taps a suggestion in the dropdown:
     *  1. Retrieve the CatalogItem (which carries the full pipe-separated record).
     *  2. Show fullDisplay in the result TextView.
     *  3. Put fullDisplay into the search bar with setText(..., false) — the
     *     'false' flag tells AutoCompleteTextView NOT to run another filter pass,
     *     so the adapter is completely unaffected.
     */
    private void bindSearchSelect(AutoCompleteTextView actv, TextView tvResult) {
        actv.setOnItemClickListener((parent, view, position, id) -> {
            CatalogItem selected = (CatalogItem) parent.getItemAtPosition(position);
            if (selected == null) return;

            tvResult.setText("USER DETAILS:\n" + selected.fullDisplay);
            // false → suppress re-filtering; the adapter's state is untouched
            actv.setText(selected.fullDisplay, false);
        });
    }
}