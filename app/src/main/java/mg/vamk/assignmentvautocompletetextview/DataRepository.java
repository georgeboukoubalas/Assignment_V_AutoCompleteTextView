package mg.vamk.assignmentvautocompletetextview;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all persistence for user submissions.
 *
 * Storage format — SharedPreferences key "submissions", value = JSON array:
 * [
 *   { "first":"John", "last":"Doe", "phone":"0401234567", "edu":"University", "hobbies":"Sports Music" },
 *   ...
 * ]
 *
 * No external libraries needed — uses only android.content and org.json (built-in).
 */
public class DataRepository {

    private static final String PREFS_NAME = "catalog_prefs";
    private static final String KEY_SUBMISSIONS = "submissions";

    // ── Keys inside each JSON object ──────────────────────────────────────────
    private static final String K_FIRST   = "first";
    private static final String K_LAST    = "last";
    private static final String K_PHONE   = "phone";
    private static final String K_EDU     = "edu";
    private static final String K_HOBBIES = "hobbies";

    private final SharedPreferences prefs;

    public DataRepository(Context context) {
        // Use application context to avoid Activity leaks
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Persist a new submission.
     */
    public void save(String first, String last, String phone, String edu, String hobbies) {
        try {
            JSONArray array = loadRawArray();
            JSONObject obj = new JSONObject();
            obj.put(K_FIRST,   first);
            obj.put(K_LAST,    last);
            obj.put(K_PHONE,   phone);
            obj.put(K_EDU,     edu);
            obj.put(K_HOBBIES, hobbies);
            array.put(obj);
            prefs.edit().putString(KEY_SUBMISSIONS, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all stored submissions as a list of Submission POJOs.
     */
    public List<Submission> loadAll() {
        List<Submission> result = new ArrayList<>();
        try {
            JSONArray array = loadRawArray();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                result.add(new Submission(
                        obj.optString(K_FIRST),
                        obj.optString(K_LAST),
                        obj.optString(K_PHONE),
                        obj.optString(K_EDU),
                        obj.optString(K_HOBBIES)
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Wipe all saved submissions (useful for testing).
     */
    public void clearAll() {
        prefs.edit().remove(KEY_SUBMISSIONS).apply();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private JSONArray loadRawArray() {
        String raw = prefs.getString(KEY_SUBMISSIONS, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // ── Submission POJO ───────────────────────────────────────────────────────

    /**
     * Immutable snapshot of one submission.
     */
    public static class Submission {
        public final String first;
        public final String last;
        public final String phone;
        public final String edu;
        public final String hobbies;

        public Submission(String first, String last, String phone, String edu, String hobbies) {
            this.first   = first;
            this.last    = last;
            this.phone   = phone;
            this.edu     = edu;
            this.hobbies = hobbies;
        }

        /** Produces the pipe-separated display string used throughout the app. */
        public String toFullDisplay() {
            return first + "|" + last + "|" + phone + "|" + edu + "|" + hobbies;
        }
    }
}
