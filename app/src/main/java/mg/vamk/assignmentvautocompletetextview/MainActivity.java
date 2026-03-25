package mg.vamk.assignmentvautocompletetextview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // We keep the list, but we will primarily use the adapter to manage data
    private ArrayList<String> catalogData = new ArrayList<>();
    private ArrayAdapter<String> adapter;

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
        final AutoCompleteTextView actvSearch = findViewById(R.id.actv_search);
        Button btnSubmit = findViewById(R.id.btn_submit);

        // 1. Setup Education Spinner
        ArrayAdapter<CharSequence> eduAdapter = ArrayAdapter.createFromResource(this,
                R.array.edu_levels, android.R.layout.simple_spinner_item);
        eduAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEdu.setAdapter(eduAdapter);

        // 2. Setup AutoComplete Adapter
        // Important: We initialize with an empty list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, catalogData);
        actvSearch.setAdapter(adapter);

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

            if (!f.isEmpty() && !l.isEmpty() && !p.isEmpty()) {
                // We create the "clean" data string for display
                String fullInfo = "First name=" + f + ", Last name=" + l + ", Phone=" + p +
                        ", Edu=" + edu + ", Hobbies=" + hobbies.toString().trim();

                /* BUG FIX: We use adapter.add() instead of catalogData.add().
                   This forces the AutoCompleteTextView to refresh its internal search filter
                   for every new submission.
                */
                adapter.add(f + " | " + l + " | " + p); // Search by First Name
                adapter.add(l + " | " + f + " | " + p); // Search by Last Name
                adapter.add(p + " | " + f + " | " + l); // Search by Phone
                adapter.add(edu + " | " + f + " " + l); // Search by Education Level

                Toast.makeText(this, "Data Saved to Catalog", Toast.LENGTH_SHORT).show();

                // Clear fields for next submission
                etFirst.setText(""); etLast.setText(""); etPhone.setText("");
                cbSports.setChecked(false); cbReading.setChecked(false); cbMusic.setChecked(false);
            } else {
                Toast.makeText(this, "Please fill Name and Phone", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Search Selection Logic
        actvSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);

            // Display the selection clearly as requested
            tvResult.setText("USER DETAILS:\n" + selection);

            // Clear search field after selection to allow new search
            actvSearch.setText("");
        });
    }
}